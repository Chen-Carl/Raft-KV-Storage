package com.zoecll.raftrpc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import io.grpc.stub.StreamObserver;
import protobuf.RaftRPCGrpc;
import protobuf.RaftRPCGrpc.RaftRPCStub;
import protobuf.RaftRPCProto.AppendEntriesRequest;
import protobuf.RaftRPCProto.AppendEntriesResponse;
import protobuf.RaftRPCProto.LogEntry;
import protobuf.RaftRPCProto.RequestVoteRequest;
import protobuf.RaftRPCProto.RequestVoteResponse;

public class RaftNode extends RaftRPCServer {

    enum RaftState {
        Follower,
        Candidate,
        Leader
    }

    class ElectionTask extends Thread {

        private final int node;
        private final RequestVoteRequest request;

        public ElectionTask(RequestVoteRequest request, int node) {
            this.request = request;
            this.node = node;
        }

        @Override
        public void run() {
            ListenableFuture<RequestVoteResponse> future = sendRequestVote(request, node);
            try {
                RequestVoteResponse response = future.get();
                if (response.getTerm() > currentTerm) {
                    convertToFollower(response.getTerm(), -1);
                    return;
                }

                if (state != RaftState.Candidate || response.getTerm() != currentTerm) {
                    return;
                }

                if (response.getVoteGranted()) {
                    totalVotes++;
                    if (state == RaftState.Candidate && totalVotes > peers.size() / 2) {
                        logger.info("[Raft node {}] Got {}/{} votes, convert to leader", id, totalVotes, peers.size());
                        convertToLeader();
                    }
                }

            } catch (InterruptedException | ExecutionException e) {
                logger.warn("[Raft node {}] Failed to get response from node {}", id, node);
            }

        }
    }

    class AppendEntriesTask extends Thread {

        private AppendEntriesRequest request;
        private final int node;

        public AppendEntriesTask(AppendEntriesRequest request, int node) {
            this.request = request;
            this.node = node;
        }

        @Override
        public void run() {
            if (state != RaftState.Leader) {
                return;
            }
            
            while (true) {
                ListenableFuture<AppendEntriesResponse> future = sendAppendEntries(request, node);
                try {
                    AppendEntriesResponse response = future.get();
                    if (response.getTerm() > currentTerm) {
                        convertToFollower(response.getTerm(), -1);
                        return;
                    }
                    if (state != RaftState.Leader || response.getTerm() != currentTerm) {
                        return;
                    }
                    if (response.getSuccess()) {
                        matchIndex.set(node, request.getPrevLogIndex() + request.getEntriesCount());
                        nextIndex.set(node, matchIndex.get(node) + 1);
                        ArrayList<Integer> sortedMatchIndex = new ArrayList<>(matchIndex);
                        sortedMatchIndex.set(id, logs.size() - 1);
                        Collections.sort(sortedMatchIndex);
                        int newCommitIndex = sortedMatchIndex.get(peers.size() / 2);
                        if (newCommitIndex > commitIndex && logs.get(newCommitIndex).getTerm() == currentTerm) {
                            commitIndex = newCommitIndex;
                            applyLogs();
                        }
                        return;
                    }

                    // if (response.getSuccess() == false)
                    nextIndex.set(node, nextIndex.get(node) - 1);
                    AppendEntriesRequest.Builder builder = AppendEntriesRequest.newBuilder();
                    builder.setTerm(currentTerm);
                    builder.setLeaderId(id);
                    builder.setPrevLogIndex(nextIndex.get(node) - 1);
                    if (nextIndex.get(node) != 0) {
                        builder.setPrevLogTerm(logs.get(nextIndex.get(node)).getTerm());
                    } else {
                        builder.setPrevLogTerm(-1);
                    }
                    builder.addAllEntries(logs.subList(nextIndex.get(node), logs.size()));
                    builder.setLeaderCommit(commitIndex);

                    request = builder.build();
                    
                } catch (InterruptedException | ExecutionException e) {
                    logger.warn("[Raft node {}] Failed to get response from node {}", id, node);
                }
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RaftNode.class);

    // common info
    private int id;
    private int totalVotes = 0;
    private RaftState state;
    private ArrayList<PeerInfo> peers;

    // persistent state on all servers
    private int currentTerm;
    private int votedFor = -1;
    private ArrayList<LogEntry> logs;

    // volatile state on all servers
    private int commitIndex;    // index of highest log entry known to be committed
    private int lastApplied;    // index of highest log entry applied to state machine

    // volatile state on leaders
    private ArrayList<Integer> nextIndex;   // the next log entry the leader will Send to that follower, initialized to 0
    private ArrayList<Integer> matchIndex;  // the highest log entry known to be replicated on that follower, initialized to -1

    private final ReadWriteLock mutex = new ReentrantReadWriteLock();
    private int electionTimeoutMin = 150;
    private int electionTimeoutMax = 300;
    private int electionTimeout = 200;
    private int heartbeat = 50;
    private long lastReceiveAppendEntries = System.currentTimeMillis();


    public RaftNode(int id, ArrayList<PeerInfo> peers) {
        this.id = id;
        this.totalVotes = 0;
        this.state = RaftState.Follower;
        this.peers = peers;

        this.currentTerm = 0;
        this.votedFor = -1;
        this.logs = new ArrayList<>();

        this.commitIndex = -1;
        this.lastApplied = -1;
        this.nextIndex = new ArrayList<>(Collections.nCopies(peers.size(), 0));
        this.matchIndex = new ArrayList<>(Collections.nCopies(peers.size(), -1));

        Yaml yaml = new Yaml();
        try {
            InputStream input = new FileInputStream("src/main/resources/config.yml");
            Map<String, Map<String, Object>> data = yaml.load(input);
            this.heartbeat = (int) data.get("raft").get("heartbeat");
            this.electionTimeoutMin = (int) data.get("raft").get("electionTimeoutMin");
            this.electionTimeoutMax = (int) data.get("raft").get("electionTimeoutMax");
        } catch (FileNotFoundException e) {
            logger.error("Node config file not found.");
            e.printStackTrace();
        }

        new Thread(() -> {
            while (true) {
                try {
                    mutex.readLock().lock();
                    RaftState state = this.state;
                    mutex.readLock().unlock();
                    switch (state) {
                        case Leader:
                            startAppendEntries();
                            try {
                                Thread.sleep(this.heartbeat);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        case Candidate:
                            startLeaderElection();
                            try {
                                Thread.sleep(electionTimeout);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        case Follower:
                            long now = System.currentTimeMillis();
                            if (now - lastReceiveAppendEntries > heartbeat * 3) {
                                try {
                                    Thread.sleep(new Random().nextInt(this.heartbeat));
                                    logger.info("[Raft node {}] No AppendEntriesRequest received, start election task", id);
                                    startLeaderElection();
                                    lastReceiveAppendEntries = System.currentTimeMillis();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                    }
                } catch (IllegalStateException e) {

                }
            }
        }).start();

    }

    @Override
    public synchronized void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        logger.debug("[Raft node {}] Received appendEntries request from {}", id, request.getLeaderId());

        AppendEntriesResponse.Builder builder = AppendEntriesResponse.newBuilder();

        if (request.getTerm() < currentTerm) {
            builder.setTerm(currentTerm);
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }
        
        electionTimeout = electionTimeoutMin + new Random().nextInt(electionTimeoutMax - electionTimeoutMin);
        convertToFollower(request.getTerm(), request.getLeaderId());
        lastReceiveAppendEntries = System.currentTimeMillis();
        
        if (request.getPrevLogIndex() == -1) {
            builder.setTerm(currentTerm);
            builder.setSuccess(true);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            logs.clear();
            logs.addAll(request.getEntriesList());
            if (request.getLeaderCommit() > commitIndex) {
                commitIndex = Math.min(request.getLeaderCommit(), logs.size() - 1);
            }
            persist();
            applyLogs();
            return;
        }

        if (request.getPrevLogIndex() > logs.size() - 1) {
            builder.setTerm(currentTerm);
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // if (request.getPrevLogIndex() <= logs.size() - 1)
        if (request.getPrevLogTerm() != logs.get(request.getPrevLogIndex()).getTerm()) {
            builder.setTerm(currentTerm);
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // if index and term are all correct, the log entry is correct
        builder.setTerm(currentTerm);
        builder.setSuccess(true);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        List<LogEntry> overdue = logs.subList(request.getPrevLogIndex() + 1, logs.size());
        logs.removeAll(overdue);
        logs.addAll(request.getEntriesList());
        if (request.getLeaderCommit() > commitIndex) {
            commitIndex = Math.min(request.getLeaderCommit(), logs.size() - 1);
        }
        persist();
        applyLogs();
    }

    @Override
    public synchronized void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        logger.debug("[Raft node {}] Received requestVote request from candidate {}", id, request.getCandidateId());

        RequestVoteResponse.Builder builder = RequestVoteResponse.newBuilder();

        if (request.getTerm() < currentTerm) {
            builder.setVoteGranted(false);
            builder.setTerm(currentTerm);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getTerm() == currentTerm) {
            if (votedFor != -1 && votedFor != request.getCandidateId()) {
                builder.setVoteGranted(false);
                builder.setTerm(currentTerm);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            int lastLogIndex = logs.size() - 1;
            int lastLogTerm = logs.size() > 0 ? logs.get(lastLogIndex).getTerm() : -1;
            if (request.getLastLogTerm() < lastLogTerm) {
                builder.setVoteGranted(false);
                builder.setTerm(currentTerm);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            if (request.getLastLogTerm() == lastLogTerm && request.getLastLogIndex() < lastLogIndex) {
                builder.setVoteGranted(false);
                builder.setTerm(currentTerm);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            // if (request.getLastLogTerm() > lastLogTerm)
            builder.setVoteGranted(true);
            builder.setTerm(currentTerm);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            votedFor = request.getCandidateId();
            persist();
            logger.debug("[Raft node {}] Voted for candidate {}", id, request.getCandidateId());
            return;
        }

        // if (request.getTerm() > currentTerm)
        logger.info("[Raft node {}] Received requestVote request from candidate {} with higher term {} > {}", id, request.getCandidateId(), request.getTerm(), currentTerm);
        convertToFollower(request.getTerm(), -1);   // update currentTerm, state, votedFor, etc.
        int lastLogIndex = logs.size() - 1;
        int lastLogTerm = logs.size() > 0 ? logs.get(lastLogIndex).getTerm() : -1;
        if (request.getLastLogTerm() < lastLogTerm) {
            builder.setVoteGranted(false);
            builder.setTerm(currentTerm);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getLastLogTerm() == lastLogTerm && request.getLastLogIndex() < lastLogIndex) {
            builder.setVoteGranted(false);
            builder.setTerm(currentTerm);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // if (request.getLastLogTerm() > lastLogTerm)
        builder.setVoteGranted(true);
        builder.setTerm(currentTerm);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        votedFor = request.getCandidateId();
        persist();
        logger.debug("[Raft node {}] Voted for candidate {}", id, request.getCandidateId());
    }
    
    private synchronized void convertToFollower(int term, int votedFor) {
        if (state != RaftState.Follower) {
            logger.info("[Raft node {}] Convert {} to follower", state.toString(), id);
        }
        currentTerm = term;
        state = RaftState.Follower;
        votedFor = -1;
        totalVotes = 0;
        persist();
    }

    private synchronized void convertToLeader() {
        if (state != RaftState.Leader) {
            logger.info("[Raft node {}] Convert {} to leader", state.toString(), id);
        }
        state = RaftState.Leader;
    }

    private synchronized void convertToCandidate() {
        if (state != RaftState.Candidate) {
            logger.info("[Raft node {}] Convert {} to candidate", state.toString(), id);
        }
        state = RaftState.Candidate;
        currentTerm++;
        votedFor = id;
        totalVotes = 1;
        electionTimeout = new Random().nextInt(2000) + 3000;
        persist();
    }

    private synchronized void persist() {

    }

    private synchronized void applyLogs() {
        while (lastApplied < commitIndex) {
            lastApplied++;
            // TODO: apply
        }
    }

    private synchronized void startLeaderElection() {
        logger.info("[Raft node {}] Starting leader election", id);

        convertToCandidate();

        int lastLogIndex = logs.size() - 1;
        int lastLogTerm = logs.size() > 0 ? logs.get(lastLogIndex).getTerm() : -1;

        RequestVoteRequest.Builder builder = RequestVoteRequest.newBuilder();
        builder.setTerm(currentTerm);
        builder.setCandidateId(id);
        builder.setLastLogIndex(lastLogIndex);
        builder.setLastLogTerm(lastLogTerm);

        for (int i = 0; i < peers.size(); i++) {
            if (i == id) {
                continue;
            }
            new ElectionTask(builder.build(), i).start();
        }
    }

    private synchronized void startAppendEntries() {
        logger.info("[Raft node {}] Starting appendEntries", id);

        for (int i = 0; i < peers.size(); i++) {
            if (i == id) {
                continue;
            }

            AppendEntriesRequest.Builder builder = AppendEntriesRequest.newBuilder();
            builder.setTerm(currentTerm);
            builder.setLeaderId(id);
            builder.setPrevLogIndex(nextIndex.get(i) - 1);
            if (nextIndex.get(i) != 0) {
                builder.setPrevLogTerm(logs.get(nextIndex.get(i)).getTerm());
            } else {
                builder.setPrevLogTerm(-1);
            }
            builder.addAllEntries(logs.subList(nextIndex.get(i), logs.size()));
            builder.setLeaderCommit(commitIndex);
            new AppendEntriesTask(builder.build(), i).start();
        }
    }

    private ListenableFuture<RequestVoteResponse> sendRequestVote(RequestVoteRequest request, int node) {
        logger.debug("[Raft node {}] Send requestVote request to node {}", id, node);

        final RaftRPCStub asyncClient = RaftRPCGrpc.newStub(peers.get(node).getChannel());
        SettableFuture<RequestVoteResponse> futureResponse = SettableFuture.create();
        asyncClient.requestVote(request, new StreamObserver<RequestVoteResponse>() {
            @Override
            public void onNext(RequestVoteResponse response) {
                futureResponse.set(response);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("[Raft node {}] Failed to sending requestVote request to node {}", id, node);
                futureResponse.setException(t);
            }

            @Override
            public void onCompleted() {

            }
        });
        return futureResponse;
    }

    private ListenableFuture<AppendEntriesResponse> sendAppendEntries(AppendEntriesRequest request, int node) {
        logger.debug("[Raft node {}] Send appendEntries request to node {}", id, node);

        final RaftRPCStub asyncClient = RaftRPCGrpc.newStub(peers.get(node).getChannel());
        SettableFuture<AppendEntriesResponse> futureResponse = SettableFuture.create();
        asyncClient.appendEntries(request, new StreamObserver<AppendEntriesResponse>() {
            @Override
            public void onNext(AppendEntriesResponse response) {
                futureResponse.set(response);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("[Raft node {}] Failed to sending appendEntries request to node {}", id, node);
                futureResponse.setException(t);
            }

            @Override
            public void onCompleted() {

            }
        });

        return futureResponse;
    }
}

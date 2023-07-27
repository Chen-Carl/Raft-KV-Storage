package com.zoecll.raftrpc;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.zoecll.config.PeerInfo;
import com.zoecll.kvstorage.KvServer;
import com.zoecll.persistence.FilePersister;
import com.zoecll.persistence.Snapshot;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import protobuf.RaftRPCGrpc;
import protobuf.RaftRPCGrpc.RaftRPCStub;
import protobuf.RaftRPCProto.AppendEntriesRequest;
import protobuf.RaftRPCProto.AppendEntriesResponse;
import protobuf.RaftRPCProto.InstallSnapshotRequest;
import protobuf.RaftRPCProto.InstallSnapshotResponse;
import protobuf.RaftRPCProto.LogEntry;
import protobuf.RaftRPCProto.RequestVoteRequest;
import protobuf.RaftRPCProto.RequestVoteResponse;

public class RaftNode {

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
                synchronized (mutex) {
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
            synchronized (mutex) {
                if (state != RaftState.Leader) {
                    return;
                }
            }
                
            while (true) {
                ListenableFuture<AppendEntriesResponse> future = sendAppendEntries(request, node);
                try {
                    AppendEntriesResponse response = future.get();
                    synchronized (mutex) {
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
                            sortedMatchIndex.set(id, getMaxLogIndex());
                            Collections.sort(sortedMatchIndex);
                            int newCommitIndex = sortedMatchIndex.get(peers.size() / 2);
                            if (newCommitIndex > commitIndex && getLogByIndex(newCommitIndex).getTerm() == currentTerm) {
                                commitIndex = newCommitIndex;
                                applyLogs();
                            }
                            return;
                        }
                        
                        AppendEntriesRequest.Builder builder = AppendEntriesRequest.newBuilder();
                        nextIndex.set(node, nextIndex.get(node) - 1);
                        builder.setTerm(currentTerm);
                        builder.setLeaderId(id);
                        builder.setPrevLogIndex(nextIndex.get(node) - 1);
                        builder.setPrevLogTerm(getLastLogTerm());
                        builder.addAllEntries(logs.subList(nextIndex.get(node) - lastIncludedIndex - 1, logs.size()));
                        builder.setLeaderCommit(commitIndex);
                        request = builder.build();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.warn("[Raft node {}] Failed to get response from node {}", id, node);
                }
            }
        }
    }

    class InstallSnapshotTask extends Thread {
        private InstallSnapshotRequest request;
        private final int node;

        public InstallSnapshotTask(InstallSnapshotRequest request, int node) {
            this.request = request;
            this.node = node;
        }

        @Override
        public void run() {
            synchronized (mutex) {
                if (state != RaftState.Leader) {
                    return;
                }
            }
            ListenableFuture<InstallSnapshotResponse> future = sendInstallSnapshot(request, node);
            try {
                InstallSnapshotResponse response = future.get();
                if (response.getTerm() > currentTerm) {
                    convertToFollower(response.getTerm(), -1);
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("[Raft node {}] Failed to get response from node {}", id, node);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RaftNode.class);

    // server components
    RaftRPCServer raftRPCServer = new RaftRPCServer(this);
    @Getter
    KvServer kvServer = new KvServer(this);

    // common info
    @Getter
    private final int id;
    @Getter
    private final ArrayList<PeerInfo> peers;
    private int totalVotes;
    private RaftState state;

    // persistent state on all servers
    private int currentTerm;
    private int votedFor;
    private ArrayList<LogEntry> logs;

    // volatile state on all servers
    private int commitIndex;    // index of highest log entry known to be committed
    private int lastApplied;    // index of highest log entry applied to state machine

    // volatile state on leaders
    private ArrayList<Integer> nextIndex;   // the next log entry the leader will Send to that follower, initialized to 0
    private ArrayList<Integer> matchIndex;  // the highest log entry known to be replicated on that follower, initialized to -1

    // log compression
    private int lastIncludedIndex;  // the snapshot replaces all entries up through and including this index
    private int lastIncludedTerm;   // term of lastIncludedIndex
    @Getter
    private FilePersister persister;

    private final ReentrantLock mutex = new ReentrantLock();
    private int electionTimeoutMin = 150;
    private int electionTimeoutMax = 300;
    private int electionTimeout = 200;
    private int heartbeat = 50;
    private int maxLogSize = 1000;
    @Setter
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

        this.lastIncludedIndex = -1;
        this.lastIncludedTerm = -1;
        this.persister = new FilePersister();

        Yaml yaml = new Yaml();
        try {
            InputStream input = new FileInputStream("src/main/resources/config.yml");
            Map<String, Map<String, Object>> data = yaml.load(input);
            Map<String, Integer> timeout = (Map<String, Integer>) data.get("cluster").get("timeout");
            this.heartbeat = timeout.get("heartbeat");
            this.electionTimeoutMin = timeout.get("electionTimeoutMin");
            this.electionTimeoutMax = timeout.get("electionTimeoutMax");
            this.maxLogSize = (int) data.get("cluster").get("maxLogSize");
        } catch (FileNotFoundException e) {
            logger.error("Node config file not found.");
            e.printStackTrace();
        }
    }

    public void lock() {
        mutex.lock();
    }

    public void unlock() {
        mutex.unlock();
    }

    @Synchronized("mutex")
    public synchronized void convertToFollower(int term, int votedFor) {
        if (state != RaftState.Follower) {
            logger.info("[Raft node {}] Convert {} to follower", state.toString(), id);
        }

        currentTerm = term;
        state = RaftState.Follower;
        votedFor = -1;
        totalVotes = 0;
    }

    @Synchronized("mutex")
    private synchronized void convertToLeader() {
        if (state != RaftState.Leader) {
            logger.info("[Raft node {}] Convert {} to leader", id, state.toString());
        }

        state = RaftState.Leader;
    }

    @Synchronized("mutex")
    private synchronized void convertToCandidate() {
        if (state != RaftState.Candidate) {
            logger.info("[Raft node {}] Convert {} to candidate", id, state.toString());
        }

        state = RaftState.Candidate;
        currentTerm++;
        votedFor = id;
        totalVotes = 1;
        electionTimeout = new Random().nextInt(2000) + 3000;
    }

    @Synchronized("mutex")
    public void applyLogs() {
        int applied = commitIndex - lastApplied;
        while (lastApplied < commitIndex) {
            lastApplied++;
            kvServer.applyLog(getLogByIndex(lastApplied).getCommand());
        }
        if (applied > 0) {
            logger.debug("[Raft node {}] Apply {} logs to state machine, lastApplied: {}", id, applied, lastApplied);
        }
    }

    @Synchronized("mutex")
    public boolean appendEntry(String command) {
        if (state != RaftState.Leader) {
            return false;
        }
        LogEntry.Builder builder = LogEntry.newBuilder();
        builder.setTerm(currentTerm);
        builder.setCommand(command);
        logs.add(builder.build());
        nextIndex.set(id, getMaxLogIndex() + 1);
        matchIndex.set(id, getMaxLogIndex());
        startAppendEntries();
        return true;
    }

    @Synchronized("mutex")
    private void startLeaderElection() {
        logger.info("[Raft node {}] Starting leader election", id);

        convertToCandidate();
        int lastLogIndex = getMaxLogIndex();
        int lastLogTerm = getLastLogTerm();

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

    @Synchronized("mutex")
    private void startAppendEntries() {
        logger.info("[Raft node {}] Starting appendEntries, current term: {}, log size: {}, commit index: {}", id, currentTerm, logs.size(), commitIndex);

        for (int i = 0; i < peers.size(); i++) {
            if (i == id) {
                continue;
            }

            AppendEntriesRequest.Builder builder = AppendEntriesRequest.newBuilder();
            builder.setTerm(currentTerm);
            builder.setLeaderId(id);
            builder.setPrevLogIndex(nextIndex.get(i) - 1);
            if (nextIndex.get(i) - lastIncludedIndex - 2 < 0) {
                builder.setPrevLogTerm(lastIncludedTerm);
            } else {
                builder.setPrevLogTerm(getLogByIndex(nextIndex.get(i) - 1).getTerm());
            }
            builder.addAllEntries(logs.subList(nextIndex.get(i) - lastIncludedIndex - 1, logs.size()));
            builder.setLeaderCommit(commitIndex);
            new AppendEntriesTask(builder.build(), i).start();
        }
    }

    @Synchronized("mutex")
    private void startInstallSnapshot() {
        Snapshot snapshot = new Snapshot(1024);
        int installIndex = Math.min(Math.min(commitIndex, Collections.min(nextIndex) - 1), lastApplied);
        snapshot.setLastIncludedIndex(installIndex);
        snapshot.setLastIncludedTerm(getLogByIndex(installIndex).getTerm());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            ArrayList<LogEntry> entries = new ArrayList<>(logs.subList(0, installIndex - lastIncludedIndex));
            oos.writeObject(entries);
        } catch (IOException e) {
            logger.error("Snapshot serialization failed");
            e.printStackTrace();
            return;
        }
        snapshot.setData(baos.toByteArray());
        logger.info("[Raft node {}] Starting installSnapshot, it will clear {} logs", id, installIndex - lastIncludedIndex);
        logs.subList(0, installIndex - lastIncludedIndex).clear();
        lastIncludedIndex = installIndex;
        lastIncludedTerm = currentTerm;

        InstallSnapshotRequest.Builder builder = InstallSnapshotRequest.newBuilder();
        builder.setTerm(currentTerm);
        builder.setLeaderId(id);
        builder.setLastIncludedIndex(snapshot.getLastIncludedIndex());
        builder.setLastIncludedTerm(snapshot.getLastIncludedTerm());
        builder.setOffset(0);
        builder.setData(ByteString.copyFrom(snapshot.getData()));
        builder.setDone(true);


        for (int i = 0; i < peers.size(); i++) {
            if (i == id) {
                continue;
            }
            new InstallSnapshotTask(builder.build(), i).start();
        }
    }

    private ListenableFuture<RequestVoteResponse> sendRequestVote(RequestVoteRequest request, int node) {
        logger.debug("[Raft node {}] Send requestVote request to node {}", id, node);

        final RaftRPCStub asyncClient = RaftRPCGrpc.newStub(peers.get(node).getRpcChannel());
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

        final RaftRPCStub asyncClient = RaftRPCGrpc.newStub(peers.get(node).getRpcChannel());
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

    private ListenableFuture<InstallSnapshotResponse> sendInstallSnapshot(InstallSnapshotRequest request, int node) {
        logger.debug("[Raft node {}] Send installSnapshot request to node {}", id, node);

        final RaftRPCStub asyncClient = RaftRPCGrpc.newStub(peers.get(node).getRpcChannel());
        SettableFuture<InstallSnapshotResponse> futureResponse = SettableFuture.create();
        asyncClient.installSnapshot(request, new StreamObserver<InstallSnapshotResponse>() {
            @Override
            public void onNext(InstallSnapshotResponse response) {
                futureResponse.set(response);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("[Raft node {}] Failed to sending installSnapshot request to node {}", id, node);
                futureResponse.setException(t);
            }

            @Override
            public void onCompleted() {

            }
        });

        return futureResponse;
    }

    @Synchronized("mutex")
    public int getCurrentTerm() {
        return currentTerm;
    }

    @Synchronized("mutex")
    public int getCommitIndex() {
        return commitIndex;
    }

    @Synchronized("mutex")
    public void setCommitIndex(int index) {
        commitIndex = index;
    }

    @Synchronized("mutex")
    public int getLastApplied() {
        return lastApplied;
    }

    @Synchronized("mutex")
    public void setLastApplied(int index) {
        lastApplied = index;
    }

    @Synchronized("mutex")
    public LogEntry getLogByIndex(int index) {
        return logs.get(index - lastIncludedIndex - 1);
    }

    @Synchronized("mutex")
    public int getLastIncludedIndex() {
        return lastIncludedIndex;
    }

    @Synchronized("mutex")
    public ArrayList<LogEntry> getLogs() {
        return logs;
    }

    @Synchronized("mutex")
    public void setLastIncludedIndex(int index) {
        lastIncludedIndex = index;
    }
    
    @Synchronized("mutex")
    public int getLastIncludedTerm() {
        return lastIncludedTerm;
    }

    @Synchronized("mutex")
    public void setLastIncludedTerm(int term) {
        lastIncludedTerm = term;
    }

    @Synchronized("mutex")
    public int getVotedFor() {
        return votedFor;
    }

    @Synchronized("mutex")
    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }

    @Synchronized("mutex")
    public int getMaxLogIndex() {
        return logs.size() + lastIncludedIndex;
    }

    @Synchronized("mutex")
    public void clearLogs(int beginIndex, int endIndex) {
        logs.subList(beginIndex - lastIncludedIndex - 1, endIndex - lastIncludedIndex - 1).clear();
    }

    @Synchronized("mutex")
    public void clearLogs(int beginIndex) {
        logs.subList(beginIndex - lastIncludedIndex - 1, logs.size()).clear();
    }

    @Synchronized("mutex")
    public void clearLogs() {
        logs.clear();
    }

    @Synchronized("mutex")
    public void addLogs(List<LogEntry> entries) {
        logs.addAll(entries);
    }

    @Synchronized("mutex")
    public int getLastLogTerm() {
        if (logs.size() != 0) {
            return logs.get(logs.size() - 1).getTerm();
        }
        if (lastIncludedIndex != -1) {
            return lastIncludedTerm;
        }
        return -1;
    }

    @Synchronized("mutex")
    public void setRandomElectionTimeout() {
        electionTimeout = electionTimeoutMin + new Random().nextInt(electionTimeoutMax - electionTimeoutMin);
    }

    public void start() {
        // start rpc server
        new Thread(() -> {
            raftRPCServer.start();
        }).start();

        // start kvstorage server
        new Thread(() -> {
            kvServer.start();
        }).start();

        // start raft server
        new Thread(() -> {
            while (true) {
                try {
                    final RaftState loopState;
                    synchronized (mutex) {
                        loopState = this.state;
                    }
                    switch (loopState) {
                        case Leader:
                            startAppendEntries();
                            synchronized (mutex) {
                                if (lastApplied - lastIncludedIndex > maxLogSize) {
                                    startInstallSnapshot();
                                }
                            }
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
}

package com.zoecll.raftrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import protobuf.RaftRPCGrpc.RaftRPCImplBase;
import protobuf.RaftRPCProto.AppendEntriesRequest;
import protobuf.RaftRPCProto.AppendEntriesResponse;
import protobuf.RaftRPCProto.InstallSnapshotRequest;
import protobuf.RaftRPCProto.InstallSnapshotResponse;
import protobuf.RaftRPCProto.RequestVoteRequest;
import protobuf.RaftRPCProto.RequestVoteResponse;

public class RaftRPCServer extends RaftRPCImplBase {

    private final static Logger logger = LoggerFactory.getLogger(RaftRPCServer.class);

    private RaftNode raftNode;

    public RaftRPCServer(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        logger.debug("[Raft node {}] Received appendEntries request from node {}", raftNode.getId(), request.getLeaderId());

        AppendEntriesResponse.Builder builder = AppendEntriesResponse.newBuilder();

        if (request.getTerm() < raftNode.getCurrentTerm()) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            logger.debug("[Raft node {}] Reject appendEntries request from node {} with lower term {} < current node term {}", raftNode.getId(), request.getLeaderId(), request.getTerm(), raftNode.getCurrentTerm());
            return;
        }

        raftNode.setRandomElectionTimeout();
        raftNode.convertToFollower(request.getTerm(), request.getLeaderId());
        raftNode.setLastReceiveAppendEntries(System.currentTimeMillis());
        
        if (request.getPrevLogIndex() == -1) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(true);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            raftNode.clearLogs();
            raftNode.addLogs(request.getEntriesList());
            if (request.getLeaderCommit() > raftNode.getCommitIndex()) {
                raftNode.setCommitIndex(Math.min(request.getLeaderCommit(), raftNode.getMaxLogIndex()));
            }
            raftNode.applyLogs();
            return;
        }

        if (request.getPrevLogIndex() > raftNode.getMaxLogIndex()) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            logger.debug("[Raft node {}] Reject appendEntries request from node {} with prevLogIndex {} > current node log index {}", raftNode.getId(), request.getLeaderId(), request.getPrevLogIndex(), raftNode.getMaxLogIndex());
            return;
        }

        // if (request.getPrevLogIndex() <= logs.size() - 1)
        int prevLogTerm = raftNode.getLastIncludedTerm();
        if (request.getPrevLogIndex() - raftNode.getLastIncludedIndex() - 1 >= 0) {
            prevLogTerm = raftNode.getLogByIndex(request.getPrevLogIndex()).getTerm();
        }
        if (request.getPrevLogTerm() != prevLogTerm) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            logger.debug("[Raft node {}] Reject appendEntries request from node {} with prevLogTerm {} != current node log term {}", raftNode.getId(), request.getLeaderId(), request.getPrevLogTerm(), raftNode.getLogByIndex(request.getPrevLogIndex()).getTerm());
            return;
        }

        // if index and term are all correct, the log entry is correct
        builder.setTerm(raftNode.getCurrentTerm());
        builder.setSuccess(true);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        raftNode.clearLogs(request.getPrevLogIndex() + 1);
        raftNode.addLogs(request.getEntriesList());
        if (request.getLeaderCommit() > raftNode.getCommitIndex()) {
            raftNode.setCommitIndex(Math.min(request.getLeaderCommit(), raftNode.getMaxLogIndex()));
        }
        if (request.getEntriesList().size() > 0) {
            logger.info("[Raft node {}] Append {} entries, log index: {}, log size: {}", raftNode.getId(), request.getEntriesList().size(), raftNode.getMaxLogIndex(), raftNode.getLogs().size());
        }
        raftNode.applyLogs();
    }

    @Override
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        logger.debug("[Raft node {}] Received requestVote request from candidate {}", raftNode.getId(), request.getCandidateId());

        RequestVoteResponse.Builder builder = RequestVoteResponse.newBuilder();

        if (request.getTerm() < raftNode.getCurrentTerm()) {
            builder.setVoteGranted(false);
            builder.setTerm(raftNode.getCurrentTerm());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getTerm() == raftNode.getCurrentTerm()) {
            if (raftNode.getVotedFor() != -1 && raftNode.getVotedFor() != request.getCandidateId()) {
                builder.setVoteGranted(false);
                builder.setTerm(raftNode.getCurrentTerm());
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            int lastLogIndex = raftNode.getMaxLogIndex();
            int lastLogTerm = raftNode.getLastLogTerm();
            if (request.getLastLogTerm() < lastLogTerm) {
                builder.setVoteGranted(false);
                builder.setTerm(raftNode.getCurrentTerm());
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            if (request.getLastLogTerm() == lastLogTerm && request.getLastLogIndex() < lastLogIndex) {
                builder.setVoteGranted(false);
                builder.setTerm(raftNode.getCurrentTerm());
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            // if (request.getLastLogTerm() > lastLogTerm)
            builder.setVoteGranted(true);
            builder.setTerm(raftNode.getCurrentTerm());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            raftNode.setVotedFor(request.getCandidateId());
            logger.debug("[Raft node {}] Voted for candidate {}", raftNode.getId(), request.getCandidateId());
            return;
        }

        // if (request.getTerm() > currentTerm)
        logger.info("[Raft node {}] Received requestVote request from candidate {} with higher term {} > {}", raftNode.getId(), request.getCandidateId(), request.getTerm(), raftNode.getCurrentTerm());
        raftNode.convertToFollower(request.getTerm(), -1);   // update currentTerm, state, votedFor, etc.
        int lastLogIndex = raftNode.getMaxLogIndex();
        int lastLogTerm = raftNode.getLastLogTerm();
        if (request.getLastLogTerm() < lastLogTerm) {
            builder.setVoteGranted(false);
            builder.setTerm(raftNode.getCurrentTerm());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getLastLogTerm() == lastLogTerm && request.getLastLogIndex() < lastLogIndex) {
            builder.setVoteGranted(false);
            builder.setTerm(raftNode.getCurrentTerm());
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // if (request.getLastLogTerm() > lastLogTerm)
        builder.setVoteGranted(true);
        builder.setTerm(raftNode.getCurrentTerm());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        raftNode.setVotedFor(request.getCandidateId());
        logger.debug("[Raft node {}] Voted for candidate {}", raftNode.getId(), request.getCandidateId());
    }

    @Override
    public void installSnapshot(InstallSnapshotRequest request, StreamObserver<InstallSnapshotResponse> responseObserver) {
        InstallSnapshotResponse.Builder builder = InstallSnapshotResponse.newBuilder();
        builder.setTerm(raftNode.getCurrentTerm());
        
        // 1. Reply immediately if term < currentTerm
        if (request.getTerm() < raftNode.getCurrentTerm()) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }
        
        if (request.getLastIncludedIndex() <= raftNode.getLastIncludedIndex()) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }
        
        // 2. Create new snapshot file if first chunk (offset is 0)
        if (request.getOffset() == 0) {
            raftNode.getPersister().createSnapshot(1024 * 1024);
        }

        // 3. Write data into snapshot file at given offset
        raftNode.getPersister().write(request.getData().toByteArray(), request.getOffset());
        
        // 4. Reply and wait for more data chunks if done is false
        if (!request.getDone()) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // 5. Save snapshot file, discard any existing or partial snapshot with a smaller index
        raftNode.getPersister().setLastIncludedIndex(request.getLastIncludedIndex());
        raftNode.getPersister().setLastIncludedTerm(request.getLastIncludedTerm());
        raftNode.setLastIncludedIndex(request.getLastIncludedIndex());
        raftNode.setLastIncludedTerm(request.getLastIncludedTerm());
        raftNode.setLastApplied(request.getLastIncludedIndex());
        raftNode.getPersister().saveSnapshot();

        // 6. If existing log entry has same index and term as snapshot’s last included entry, retain log entries following it and reply
        int requestLastIncludedTerm = request.getLastIncludedTerm();
        int localLastIncludedTerm = raftNode.getLastIncludedTerm();
        
        if (request.getLastIncludedIndex() < raftNode.getMaxLogIndex() && requestLastIncludedTerm == localLastIncludedTerm) {
            raftNode.clearLogs(request.getLastIncludedIndex() + 1, request.getLastIncludedIndex() + 1);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }
        
        // 7. Discard the entire log
        raftNode.clearLogs();

        // 8. Reset state machine using snapshot contents (and load snapshot’s cluster configuration)
        raftNode.getKvServer().reset(raftNode.getPersister());
    }

    void start() {
        Server rpcServer = ServerBuilder.forPort(raftNode.getPeers().get(raftNode.getId()).getRpcPort()).addService(this).build();
        try {
            rpcServer.start();
            rpcServer.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

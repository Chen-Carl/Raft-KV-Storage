package com.zoecll.raftrpc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import protobuf.RaftRPCGrpc.RaftRPCImplBase;
import protobuf.RaftRPCProto.AppendEntriesRequest;
import protobuf.RaftRPCProto.AppendEntriesResponse;
import protobuf.RaftRPCProto.LogEntry;
import protobuf.RaftRPCProto.RequestVoteRequest;
import protobuf.RaftRPCProto.RequestVoteResponse;

public class RaftRPCServer extends RaftRPCImplBase {

    private final static Logger logger = LoggerFactory.getLogger(RaftRPCServer.class);

    private RaftNode raftNode;

    public RaftRPCServer(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    @Override
    public synchronized void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        logger.debug("[Raft node {}] Received appendEntries request from node {}", raftNode.getId(), request.getLeaderId());

        AppendEntriesResponse.Builder builder = AppendEntriesResponse.newBuilder();

        if (request.getTerm() < raftNode.getCurrentTerm()) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
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
            raftNode.getLogs().clear();
            raftNode.getLogs().addAll(request.getEntriesList());
            if (request.getLeaderCommit() > raftNode.getCommitIndex()) {
                raftNode.setCommitIndex(Math.min(request.getLeaderCommit(), raftNode.getLogs().size() - 1));
            }
            raftNode.persist();
            raftNode.applyLogs();
            return;
        }

        if (request.getPrevLogIndex() > raftNode.getLogs().size() - 1) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // if (request.getPrevLogIndex() <= logs.size() - 1)
        if (request.getPrevLogTerm() != raftNode.getLogs().get(request.getPrevLogIndex()).getTerm()) {
            builder.setTerm(raftNode.getCurrentTerm());
            builder.setSuccess(false);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        // if index and term are all correct, the log entry is correct
        builder.setTerm(raftNode.getCurrentTerm());
        builder.setSuccess(true);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
        List<LogEntry> overdue = raftNode.getLogs().subList(request.getPrevLogIndex() + 1, raftNode.getLogs().size());
        raftNode.getLogs().removeAll(overdue);
        raftNode.getLogs().addAll(request.getEntriesList());
        if (request.getLeaderCommit() > raftNode.getCommitIndex()) {
            raftNode.setCommitIndex(Math.min(request.getLeaderCommit(), raftNode.getLogs().size() - 1));
        }
        if (request.getEntriesList().size() > 0) {
            logger.info("[Raft node {}] Append {} entries, log size: {}", raftNode.getId(), request.getEntriesList().size(), raftNode.getLogs().size());
        }
        raftNode.persist();
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

            int lastLogIndex = raftNode.getLogs().size() - 1;
            int lastLogTerm = raftNode.getLogs().size() > 0 ? raftNode.getLogs().get(lastLogIndex).getTerm() : -1;
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
            raftNode.persist();
            logger.debug("[Raft node {}] Voted for candidate {}", raftNode.getId(), request.getCandidateId());
            return;
        }

        // if (request.getTerm() > currentTerm)
        logger.info("[Raft node {}] Received requestVote request from candidate {} with higher term {} > {}", raftNode.getId(), request.getCandidateId(), request.getTerm(), raftNode.getCurrentTerm());
        raftNode.convertToFollower(request.getTerm(), -1);   // update currentTerm, state, votedFor, etc.
        int lastLogIndex = raftNode.getLogs().size() - 1;
        int lastLogTerm = raftNode.getLogs().size() > 0 ? raftNode.getLogs().get(lastLogIndex).getTerm() : -1;
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
        raftNode.persist();
        logger.debug("[Raft node {}] Voted for candidate {}", raftNode.getId(), request.getCandidateId());
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

package com.zoecll.raftrpc;

import io.grpc.stub.StreamObserver;
import protobuf.RaftRPCGrpc.RaftRPCImplBase;
import protobuf.RaftRPCProto.AppendEntriesRequest;
import protobuf.RaftRPCProto.AppendEntriesResponse;
import protobuf.RaftRPCProto.RequestVoteRequest;
import protobuf.RaftRPCProto.RequestVoteResponse;

public abstract class RaftRPCServer extends RaftRPCImplBase {

    @Override
    public abstract void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver);

    @Override
    public abstract void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver);
    
}

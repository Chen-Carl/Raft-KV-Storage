package com.zoecarl.raft.raftrpc;

import com.zoecarl.raft.raftrpc.service.AppendEntriesService;
import com.zoecarl.raft.raftrpc.service.RequestVoteService;
import com.zoecarl.rpc.RpcServer;

public class RaftRpcServer extends RpcServer {
    public RaftRpcServer(int port) {
        super(port);
        super.register(RequestVoteService.class);
        super.register(AppendEntriesService.class);
    }
}

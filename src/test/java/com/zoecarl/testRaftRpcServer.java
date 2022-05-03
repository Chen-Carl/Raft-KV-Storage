package com.zoecarl;

import com.zoecarl.raft.raftrpc.RaftRpcServer;

public class testRaftRpcServer {
    public static void main(String[] args) {
        RaftRpcServer server = new RaftRpcServer(13308);
        server.launch();
    }
}

package com.zoecarl;

import com.zoecarl.raft.raftrpc.RaftRpcServer;
import com.zoecarl.raft.Raft;

public class testRaftRpcServer {
    public static void main(String[] args) {
        Raft raft = new Raft("127.0.0.1", 13308);
        RaftRpcServer server = new RaftRpcServer(13308, raft);
        server.launch();
    }
}

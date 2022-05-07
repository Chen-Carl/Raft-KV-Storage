package com.zoecarl.basic;

import com.zoecarl.raft.Raft;

public class testElectionServer {
    public static void main(String[] args) {
        Raft raft = new Raft("127.0.0.1", 13302);
        raft.init();
    }
}

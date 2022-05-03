package com.zoecarl;

import com.zoecarl.raft.Raft;

public class testElectionTask {
    public static void main(String[] args) {
        Raft raft = new Raft("127.0.0.1", 13306);
        raft.init(false);
        raft.startElection();
    }
}

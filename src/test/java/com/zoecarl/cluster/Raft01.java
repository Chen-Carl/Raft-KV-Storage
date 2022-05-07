package com.zoecarl.cluster;

import com.zoecarl.raft.Raft;

public class Raft01 {
    public static void main(String[] args) {
        Raft node = new Raft();
        node.init(0, "./src/test/java/com/zoecarl/cluster/settings.txt");
    }
}

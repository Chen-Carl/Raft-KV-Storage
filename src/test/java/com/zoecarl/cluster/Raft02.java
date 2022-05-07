package com.zoecarl.cluster;

import com.zoecarl.raft.Raft;

public class Raft02 {
    public static void main(String[] args) {
        Raft node = new Raft();
        node.init(1, "./src/test/java/com/zoecarl/cluster/settings.txt");
    }
}

package com.zoecarl.cluster;

import com.zoecarl.raft.raftrpc.RaftRpcClient;

public class Client {
    public static void main(String[] args) {
        RaftRpcClient client = new RaftRpcClient("127.0.0.1", 13320, true);
        boolean success = client.put("key1", "value1");
        System.out.println(success);
        String value = client.get("key1");
        System.out.println(value);
    }
}

package com.zoecarl;

import com.zoecarl.raft.raftrpc.RaftRpcClient;
import com.zoecarl.raft.raftrpc.Request;
import com.zoecarl.raft.raftrpc.Request.RequestType;
import com.zoecarl.raft.raftrpc.Response;

public class testRaftRpcClient {
    public static void main(String[] args) {
        RaftRpcClient client = new RaftRpcClient("127.0.0.1", 13308);
        Request req = new Request(RequestType.REQUEST_VOTE, "hello");
        Response resp = client.requestVoteRpc(req);
        System.out.println(resp.getResp());
    }
}

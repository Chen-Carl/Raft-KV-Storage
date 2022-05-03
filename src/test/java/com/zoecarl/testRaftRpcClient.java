package com.zoecarl;

import com.zoecarl.raft.raftrpc.RaftRpcClient;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteResp;

public class testRaftRpcClient {
    public static void main(String[] args) {
        RaftRpcClient client = new RaftRpcClient("127.0.0.1", 13308);
        ReqVoteReq req = new ReqVoteReq(1, "127.0.0.1:13308", "127.0.0.1:13308", 1, 1);
        ReqVoteResp resp = (ReqVoteResp) client.requestVoteRpc(req);
        System.out.println(resp.getContent());
    }
}

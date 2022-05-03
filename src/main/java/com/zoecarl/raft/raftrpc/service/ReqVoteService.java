package com.zoecarl.raft.raftrpc.service;

import com.zoecarl.raft.raftrpc.common.Request;
import com.zoecarl.raft.Raft;
import com.zoecarl.raft.raftrpc.common.ReqVoteResp;
import com.zoecarl.rpc.ServiceProvider;

public class ReqVoteService implements ServiceProvider {
    public ReqVoteResp handleRequestVote(Request req, Raft selfNode) {
        ReqVoteResp res = new ReqVoteResp(0, false, "world");
        return res;
    }
}

package com.zoecarl.raft.raftrpc.service;

import com.zoecarl.raft.raftrpc.Request;
import com.zoecarl.raft.raftrpc.Response;
import com.zoecarl.raft.raftrpc.Response.ResponseType;
import com.zoecarl.rpc.ServiceProvider;

public class RequestVoteService implements ServiceProvider {
    public Response handleRequestVote(Request req) {
        Response res = new Response(ResponseType.REQUEST_VOTE, "world");
        return res;
    }
}

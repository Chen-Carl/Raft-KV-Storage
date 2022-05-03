package com.zoecarl.raft.raftrpc.service;

import com.zoecarl.raft.raftrpc.Request;
import com.zoecarl.raft.raftrpc.Response;
import com.zoecarl.raft.raftrpc.Response.ResponseType;

public class AppendEntriesService {
    public Response handleAppendEntries(Request req) {
        Response res = new Response(ResponseType.APPEND_ENTRIES, "world");
        return res;
    }
}

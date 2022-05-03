package com.zoecarl.raft.raftrpc.service;

import com.zoecarl.raft.raftrpc.common.Request;
import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.raft.raftrpc.common.AppendEntriesResp;
import com.zoecarl.raft.Raft;

public class AppendEntriesService implements ServiceProvider {
    public AppendEntriesResp handleAppendEntries(Request req, Raft selfNode) {
        AppendEntriesResp res = new AppendEntriesResp("world");
        return res;
    }
}

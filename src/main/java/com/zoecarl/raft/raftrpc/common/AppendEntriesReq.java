package com.zoecarl.raft.raftrpc.common;

public class AppendEntriesReq extends Request {
    public AppendEntriesReq(String serverId) {
        super(serverId);
    }
}

package com.zoecarl.raft.raftrpc.common;

import com.zoecarl.common.LogEntry;

public class AppendEntriesReq extends Request {
    private long term;
    private String serverId;
    private String leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private int leaderCommit;
    private LogEntry[] entries;
    
    public AppendEntriesReq(String serverId) {
        super(serverId);
    }
}

package com.zoecarl.raft.raftrpc.common;

import com.zoecarl.common.LogEntry;

public class AppendEntriesReq extends Request {
    private int term;
    private String serverId;
    private String leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private int leaderCommit;
    private LogEntry[] entries;
    
    public AppendEntriesReq(String serverId) {
        super(serverId);
    }

    public LogEntry[] getEntries() {
        return entries;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getTerm() {
        return term;
    }
}

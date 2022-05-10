package com.zoecarl.raft.raftrpc.common;

import com.zoecarl.common.LogEntry;

public class AppendEntriesReq extends Request {
    private int term;               // leader’s term
    private String leaderId;        // so follower can redirect clients
    private int prevLogIndex;       // index of log entry immediately preceding new ones
    private int prevLogTerm;        // term of prevLogIndex entry
    private LogEntry[] entries;     // log entries to store (empty for heartbeat; may send more than one for efficiency)
    private int leaderCommit;       // leader’s commitIndex
    
    public AppendEntriesReq(int term, String serverId) {
        super(serverId);
        this.term = term;
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

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    @Override
    public String toString() {
        return "AppendEntriesReq {\n\tterm=" + term + ", \n\tleaderId=" + leaderId + ", \n\tprevLogIndex=" + prevLogIndex + ", \n\tprevLogTerm=" + prevLogTerm + ", \n\tleaderCommit=" + leaderCommit + ", \n\tentries=" + entries + "\n}";
    }
}

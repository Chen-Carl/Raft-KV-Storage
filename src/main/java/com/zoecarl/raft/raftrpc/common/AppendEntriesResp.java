package com.zoecarl.raft.raftrpc.common;

public class AppendEntriesResp extends Response {
    private int term;        // currentTerm, for leader to update itself
    private boolean success; // true if follower contained entry matching prevLogIndex and prevLogTerm

    public AppendEntriesResp(int term, boolean success) {
        super();
        this.term = term;
        this.success = success;
    }

    public AppendEntriesResp(boolean success) {
        super();
        this.success = success;
    }
    
    public AppendEntriesResp(int term, boolean success, Object content) {
        super(content);
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }
}

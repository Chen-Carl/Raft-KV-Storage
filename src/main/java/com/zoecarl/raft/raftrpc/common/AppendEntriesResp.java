package com.zoecarl.raft.raftrpc.common;

public class AppendEntriesResp extends Response {
    private int term;

    public AppendEntriesResp(int term) {
        super();
        this.term = term;
    }
    
    public AppendEntriesResp(int term, Object content) {
        super(content);
        this.term = term;
    }

    public int getTerm() {
        return term;
    }
}

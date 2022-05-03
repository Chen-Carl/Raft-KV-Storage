package com.zoecarl.raft.raftrpc.common;

public class ReqVoteResp extends Response {
    private int term;
    private boolean voteGranted;

    public ReqVoteResp(int term, boolean voteGranted) {
        super();
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public ReqVoteResp(int term, boolean voteGranted, Object content) {
        super(content);
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }
}

package com.zoecarl.common;

public class ReqVoteResp {
    int term;
    boolean voteGranted;

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public int getTerm() {
        return term;
    }
}

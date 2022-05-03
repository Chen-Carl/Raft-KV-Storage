package com.zoecarl.raft.raftrpc.common;

public class ReqVoteReq extends Request {
    private int term;
    private int lastLogIndex;
    private int lastLogTerm;
    private String candidateId;

    public ReqVoteReq(int term, String serverId, String candidateId, int lastLogIndex, int lastLogTerm) {
        super(serverId);
        this.term = term;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
        this.candidateId = candidateId;
    }

    public ReqVoteReq(int term, String serverId, String candidateId, int lastLogIndex, int lastLogTerm, Object content) {
        super(serverId, content);
        this.term = term;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
        this.candidateId = candidateId;
    }

    public int getTerm() {
        return term;
    }
}

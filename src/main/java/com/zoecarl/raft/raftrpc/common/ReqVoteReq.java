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

    public String getCandidateId() {
        return candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    @Override
    public String toString() {
        return "ReqVoteReq{\n" +
                "\tterm=" + term +
                ", \n\tlastLogIndex=" + lastLogIndex +
                ", \n\tlastLogTerm=" + lastLogTerm +
                ", \n\tcandidateId='" + candidateId + '\'' +
                ", \n\tserverId='" + getHostname() + ':' + getPort() + '\'' +
                "\n}";
    }
}

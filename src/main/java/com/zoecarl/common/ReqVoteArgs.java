package com.zoecarl.common;

public class ReqVoteArgs {
    private int term;
    private String serverId;
    private String candidateId;
    private int lastLogIndex;
    private int lastLogTerm;

    public ReqVoteArgs(int term, String serverId, String candidateId, int lastLogIndex, int lastLogTerm) {
        this.term = term;
        this.serverId = serverId;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    public String getHostname() {
        return serverId.split(":")[0];
    }

    public int getPort() {
        return Integer.parseInt(serverId.split(":")[1]);
    }
}

package com.zoecarl.raft.raftrpc.service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.raft.Raft;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteResp;
import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.common.Peers;


public class ReqVoteService implements ServiceProvider {
    static final Logger logger = LogManager.getLogger("server");

    synchronized public ReqVoteResp handleRequestVote(ReqVoteReq req, Raft selfNode) {
        logger.warn("{} receive a request vote request from {}", selfNode.getPeers().getSelf(), req.getCandidateId());
        // Reply false if term < currentTerm
        if (req.getTerm() < selfNode.getCurrTerm()) {
            logger.info("refuse to vote for {}, because term {} is smaller than current term {}", req.getCandidateId(), req.getTerm(), selfNode.getCurrTerm());
            return new ReqVoteResp(selfNode.getCurrTerm(), false);
        }
        //  If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote
        if (!selfNode.getVotedFor().equals("") && !selfNode.getVotedFor().equals(req.getCandidateId())) {
            logger.info("refuse to vote for {}, because already voted for {}", req.getCandidateId(), selfNode.getVotedFor());
            return new ReqVoteResp(selfNode.getCurrTerm(), false);
        }
        if (req.getTerm() == selfNode.getCurrTerm()) {
            int lastIndex = selfNode.getLogModule().back().getIndex();
            if (lastIndex > req.getLastLogIndex()) {
                logger.info("refuse to vote for {}, because last log index is smaller than current log index", req.getCandidateId());
                return new ReqVoteResp(selfNode.getCurrTerm(), false);
            }
        }

        logger.info("vote for {}", req.getCandidateId());
        selfNode.setState(Raft.ServerState.FOLLOWER);
        Peers peers = selfNode.getPeers();
        peers.setLeader(peers.new Peer(req.getCandidateId()));
        selfNode.setCurrTerm(req.getTerm());
        selfNode.setVotedFor(req.getCandidateId());
        return new ReqVoteResp(selfNode.getCurrTerm(), true);
    }
}

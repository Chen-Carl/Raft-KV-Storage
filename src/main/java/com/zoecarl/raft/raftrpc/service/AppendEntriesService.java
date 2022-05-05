package com.zoecarl.raft.raftrpc.service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.raft.raftrpc.common.AppendEntriesReq;
import com.zoecarl.raft.raftrpc.common.AppendEntriesResp;
import com.zoecarl.raft.Raft;

public class AppendEntriesService implements ServiceProvider {
    private static final Logger logger = LogManager.getLogger(AppendEntriesService.class);

    synchronized public AppendEntriesResp handleAppendEntries(AppendEntriesReq req, Raft selfNode) {
        if (req.getEntries() == null) {
            if (req.getTerm() >= selfNode.getCurrTerm()) {
                logger.warn("receive a heartbeat from a higher term node {}, change state to FOLLOWER", req.getLeaderId());
                selfNode.setCurrTerm(req.getTerm());
                selfNode.setVotedFor(null);
                selfNode.setLeader(req.getLeaderId());
                selfNode.setState(Raft.ServerState.FOLLOWER);
                selfNode.preHeartBeatTime = System.currentTimeMillis();
                selfNode.preElectionTime = System.currentTimeMillis();
                return new AppendEntriesResp(req.getTerm());
            } else {
                return new AppendEntriesResp(selfNode.getCurrTerm());
            }
        }
        
        logger.info("receive AppendEntriesReq from " + req.getLeaderId() + " with " + req.getEntries().length + " entries");

        AppendEntriesResp res = new AppendEntriesResp(0);
        return res;
    }
}

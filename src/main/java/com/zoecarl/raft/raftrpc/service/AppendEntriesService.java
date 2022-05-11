package com.zoecarl.raft.raftrpc.service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.raft.raftrpc.common.AppendEntriesReq;
import com.zoecarl.raft.raftrpc.common.AppendEntriesResp;
import com.zoecarl.raft.Raft;
import com.zoecarl.raft.Raft.ServerState;
import com.zoecarl.common.LogEntry;

public class AppendEntriesService implements ServiceProvider {
    private static final Logger logger = LogManager.getLogger(AppendEntriesService.class);

    synchronized public AppendEntriesResp handleAppendEntries(AppendEntriesReq req, Raft selfNode) {
        // TODO: try catch
        try {
            // Reply false if term < currentTerm
            if (req.getTerm() < selfNode.getCurrTerm()) {
                logger.error(
                        "receive append entries request but failed to execute, because the term is smaller than current term");
                return new AppendEntriesResp(selfNode.getCurrTerm(), false);
            }

            selfNode.preHeartBeatTime = System.currentTimeMillis();
            selfNode.preElectionTime = System.currentTimeMillis();
            if (selfNode.state() != ServerState.FOLLOWER) {
                logger.info("node {} become FOLLOWER after receiving append entries with currentTerm={}, reqTerm={}",
                        selfNode.getSelfId(), selfNode.getCurrTerm(), req.getTerm());
            }
            selfNode.setState(ServerState.FOLLOWER);
            selfNode.setCurrTerm(req.getTerm());
            selfNode.setLeader(req.getLeaderId());

            // 1. heartbeat
            if (req.getEntries() == null) {
                logger.warn("receive a heartbeat from node {} and reply true", req.getLeaderId());
                return new AppendEntriesResp(selfNode.getCurrTerm(), true);
            }

            // 2. append log entries

            // Reply false if log doesn’t contain an entry at prevLogIndex whose term
            // matches prevLogTerm
            LogEntry prevLog = selfNode.getLogModule().read(req.getPrevLogIndex());
            if (req.getPrevLogIndex() != -1) {
                if (prevLog == null) {
                    logger.error("It does not contain an entry at {}", req.getPrevLogIndex());
                    return new AppendEntriesResp(selfNode.getCurrTerm(), false);
                }
                if (prevLog.getTerm() != req.getPrevLogTerm()) {
                    logger.error("The previous log term={} is not matched", prevLog.getTerm());
                    return new AppendEntriesResp(selfNode.getCurrTerm(), false);
                }
            }

            logger.info("This is the first log entry");

            // If an existing entry conflicts with a new one (same index but different
            // terms), delete the existing entry and all that follow it
            LogEntry existLog = selfNode.getLogModule().read(req.getPrevLogIndex() + 1);
            int entriesCount = 0;
            if (existLog != null && existLog.getTerm() != req.getEntries()[0].getTerm()) {
                selfNode.getLogModule().removeOnStartIndex(req.getPrevLogIndex() + 1);
            } else {
                // Append any new entries not already in the log
                for (int i = 1; i < req.getEntries().length; i++) {
                    selfNode.getLogModule().write(req.getEntries()[i]);
                    entriesCount++;
                }
            }

            // Append any new entries not already in the log
            for (int i = 0; i < req.getEntries().length; i++) {
                selfNode.getLogModule().write(req.getEntries()[i]);
                entriesCount++;
            }

            logger.info("node {} append {} entries to log", selfNode.getSelfId(), entriesCount);

            // If leaderCommit > commitIndex, set commitIndex = min(leaderCommit, index of
            // last new entry)
            if (req.getLeaderCommit() > selfNode.getCommitIndex()) {
                selfNode.setCommitIndex((int) Math.min(req.getLeaderCommit(), selfNode.getLogModule().size() - 1));
            }
            logger.info("write to log module successful");
            return new AppendEntriesResp(selfNode.getCurrTerm(), true);
        } catch (Exception e) {
            logger.fatal("append entries fatal error {}", e);
            return null;
        }
    }
}

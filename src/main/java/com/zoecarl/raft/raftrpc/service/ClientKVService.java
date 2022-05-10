package com.zoecarl.raft.raftrpc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.common.ClientKVResp;
import com.zoecarl.common.LogEntry;
import com.zoecarl.common.ClientKVReq.Type;
import com.zoecarl.common.ClientKVReq;
import com.zoecarl.common.Peers.Peer;
import com.zoecarl.concurr.RaftThreadPool;
import com.zoecarl.raft.Raft.ServerState;
import com.zoecarl.raft.raftrpc.common.AppendEntriesReq;
import com.zoecarl.raft.raftrpc.common.AppendEntriesResp;
import com.zoecarl.raft.Raft;

public class ClientKVService implements ServiceProvider {
    private static final Logger logger = LogManager.getLogger(ClientKVService.class);

    ClientKVResp handleClientKV(ClientKVReq req, Raft selfNode) {
        logger.info("receive {} request, (k, v) = ({}, {})", req.getTypeString(), req.getKey(), req.getValue());
        if (selfNode.state() != Raft.ServerState.LEADER) {
            logger.warn("FOLLOWER {} redirect the request to LEADER {}", selfNode.getSelfId(), selfNode.getLeaderId());
            return redirect(req, selfNode);
        }

        if (req.getType() == Type.GET) {
            LogEntry logEntry = selfNode.getStateMachine().get(req.getKey());
            if (logEntry != null) {
                return new ClientKVResp(logEntry.getKey(), logEntry.getValue());
            }
            return new ClientKVResp();
        }

        if (req.getType() == Type.PUT) {
            LogEntry logEntry = new LogEntry(selfNode.getCurrTerm(), req.getKey(), req.getValue());
            selfNode.getLogModule().write(logEntry);

            final AtomicInteger success = new AtomicInteger(0);
            ArrayList<Future<Boolean>> futures = new ArrayList<>();
            CopyOnWriteArrayList<Boolean> results = new CopyOnWriteArrayList<>();
            for (Peer peer : selfNode.getPeers().getPeerList()) {
                if (!peer.equals(selfNode.getSelf())) {
                    futures.add(replicate(peer, logEntry, selfNode));
                }
            }
            CountDownLatch latch = new CountDownLatch(futures.size());
            for (Future<Boolean> future : futures) {
                RaftThreadPool.execute(() -> {
                    try {
                        results.add(future.get(3000, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException e) {
                        logger.error("remote log replication interrupted: ", e.getMessage());
                    } catch (ExecutionException e) {
                        logger.error("remote log replication execution exception: ", e.getMessage());
                    } catch (TimeoutException e) {
                        logger.error("remote log replication timeout: ", e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }, false);
            }

            try {
                latch.await(4000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("remote log replication interrupted: ", e.getMessage());
            }

            for (Boolean res : results) {
                if (res) {
                    success.incrementAndGet();
                }
            }

            ArrayList<Integer> matchIndexList = new ArrayList<>(selfNode.matchIndex.values());
            int majority = matchIndexList.size() / 2;
            Collections.sort(matchIndexList);
            int n = matchIndexList.get(majority);
            if (n > selfNode.getCommitIndex()) {
                LogEntry entry = selfNode.getLogModule().read(n);
                if (entry != null && entry.getTerm() == selfNode.getCurrTerm()) {
                    selfNode.setCommitIndex(n);
                }
            }

            if (success.get() > selfNode.getPeers().size() / 2) {
                int commitIndex = logEntry.getIndex();
                selfNode.setCommitIndex(commitIndex);
                selfNode.getStateMachine().apply(logEntry);
                selfNode.setLastApplied(commitIndex);
                logger.info("state machine applies local log entry {}", logEntry);
                return new ClientKVResp(null, "true");
            } else {
                selfNode.getLogModule().removeOnStartIndex(logEntry.getIndex());
                logger.warn("failed to apply local state machine, logEntry {}", logEntry);
                return new ClientKVResp(null, "false");
            }
        }
        return null;
    }

    private ClientKVResp redirect(ClientKVReq req, Raft selfNode) {
        if (req.getType() == Type.PUT) {
            boolean success = selfNode.getClient().put(req.getKey(), req.getValue());
            if (success) {
                return new ClientKVResp(null, "true");
            }
            return new ClientKVResp(null, "false");
        } else {
            String res = selfNode.getClient().get(req.getKey());
            return new ClientKVResp(req.getKey(), res);
        }
    }

    private Future<Boolean> replicate(Peer peer, LogEntry logEntry, Raft selfNode) {
        return RaftThreadPool.submit(() -> {
            int nextIndex = selfNode.nextIndex.get(peer);
            ArrayList<LogEntry> logEntries = new ArrayList<>();
            if (logEntry.getIndex() >= nextIndex) {
                for (int i = nextIndex; i <= logEntry.getIndex(); i++) {
                    LogEntry entry = selfNode.getLogModule().read(i);
                    if (entry == null) {
                        logEntries.add(entry);
                    }
                }
            } else {
                logEntries.add(logEntry);
            }
            LogEntry firstLogEntry = logEntries.get(0);
            LogEntry prevLogEntry = selfNode.getLogModule().read(firstLogEntry.getIndex() - 1);
            if (prevLogEntry == null) {
                prevLogEntry = new LogEntry(0, 0, null, null);
            }

            LogEntry[] logEntriesArray = logEntries.toArray(new LogEntry[logEntries.size()]);
            int prevLogTerm = prevLogEntry.getTerm();
            int prevLogIndex = prevLogEntry.getIndex();
            AppendEntriesReq req = new AppendEntriesReq(selfNode.getCurrTerm(), peer.getAddr(), prevLogIndex,
                    selfNode.getLeaderId(), prevLogTerm, logEntriesArray, selfNode.getCommitIndex());
            AppendEntriesResp resp = selfNode.getClient().appendEntriesRpc(req);
            if (resp == null) {
                return false;
            }
            if (resp.success()) {
                logger.info("replicate log entry to {} success, entry={}", peer.getAddr(), logEntriesArray);
                selfNode.nextIndex.put(peer, logEntry.getIndex() + 1);
                selfNode.matchIndex.put(peer, logEntry.getIndex());
                return true;
            }
            if (resp.getTerm() > selfNode.getCurrTerm()) {
                logger.warn("receive a higher term log entry from {}, turn to FOLLOWER", peer.getAddr());
                selfNode.setCurrTerm(resp.getTerm());
                selfNode.setState(ServerState.FOLLOWER);
                return false;
            }
            if (nextIndex != 0) {
                nextIndex--;
            }
            selfNode.nextIndex.put(peer, nextIndex);
            logger.warn("FOLLOWER node {} log replication mismatch, nextIndex changes to {}", peer.getAddr(),
                    nextIndex);
            return false;
        });
    }
}

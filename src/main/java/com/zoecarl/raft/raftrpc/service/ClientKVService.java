package com.zoecarl.raft.raftrpc.service;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.text.StyledEditorKit.BoldAction;

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
import com.zoecarl.common.Peers;
import com.zoecarl.common.Peers.Peer;
import com.zoecarl.concurr.RaftThreadPool;
import com.zoecarl.raft.StateMachine;
import com.zoecarl.raft.LogModule;
import com.zoecarl.raft.Raft;

public class ClientKVService implements ServiceProvider {
    private static final Logger logger = LogManager.getLogger(ClientKVService.class);

    ClientKVResp handleClientKV(ClientKVReq req, Raft selfNode) {
        logger.info("receive {} request, (k, v) = ({}, {})", req.getTypeString(), req.getKey(), req.getValue());
        if (selfNode.state() != Raft.ServerState.LEADER) {
            logger.warn("FOLLOWER {} redirect the request to LEADER {}", selfNode.getSelfId(), selfNode.getLeaderId());
            // TODO: redirect
            // return redirect(req);
        }

        if (req.getType() == Type.GET) {
            LogEntry logEntry = selfNode.getStateMachine().get(req.getKey());
            if (logEntry != null) {
                return new ClientKVResp(logEntry.getKey(), logEntry.getValue());
            }
            return null;
        } 
        if (req.getType() == Type.PUT) {
            LogEntry logEntry = new LogEntry(selfNode.getCurrTerm(), req.getKey(), req.getValue());
            selfNode.getLogModule().write(logEntry);

            final AtomicInteger success = new AtomicInteger(0);
            ArrayList<Future<Boolean>> futures = new ArrayList<>();
            CopyOnWriteArrayList<Boolean> results = new CopyOnWriteArrayList<>();
            for (Peer peer : selfNode.getPeers().getPeerList()) {
                if (!peer.equals(selfNode.getSelf())) {
                    // futures.add(replicate(peer, logEntry));
                    // TODO: replicate
                }
            }
            CountDownLatch latch = new CountDownLatch(futures.size());
            for (Future<Boolean> future : futures) {
                RaftThreadPool.execute(() -> {
                    try {
                        results.add(future.get(3000, TimeUnit.MILLISECONDS)); //InterruptedException, ExecutionException, TimeoutException;
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

            if (success.get() > selfNode.getPeers().size() / 2 + 1) {

            }
        }
        return null;
    }
    
    private ClientKVResp redirect() {
        return null;
    }

    private Future<Boolean> replicate(Peer peer, LogEntry logEntry) {
        return null;
    }
}

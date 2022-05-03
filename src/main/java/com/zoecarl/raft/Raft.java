package com.zoecarl.raft;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import com.zoecarl.common.LogEntry;
import com.zoecarl.common.Peers;
import com.zoecarl.concurr.RaftThreadPool;
import com.zoecarl.raft.raftrpc.RaftRpcClient;
import com.zoecarl.raft.raftrpc.RaftRpcServer;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteResp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Raft {

    public static final Logger logger = LoggerFactory.getLogger(Raft.class);

    enum ServerState {
        FOLLOWER,
        CANDIDATE,
        LEADER
    }

    private ServerState state;
    private Peers peers;
    private int currentTerm;
    private String votedFor;
    private LogModule logModule;

    private int port;
    private String host;

    private RaftRpcClient raftRpcClient;
    private RaftRpcServer raftRpcServer;
    
    private ElectionTask electionTask = new ElectionTask();

    ConcurrentHashMap<Peers.Peer, Integer> nextIndex;
    ConcurrentHashMap<Peers.Peer, Integer> matchIndex;

    public Raft(String host, int port) {
        this.state = ServerState.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = null;
        this.host = host;
        this.port = port;
    }

    public void init(boolean launchServer) {
        raftRpcServer = new RaftRpcServer(port, this);
        raftRpcClient = new RaftRpcClient(host, port);
        if (launchServer == true) {
            raftRpcServer.launch();
        }
    }

    public Peers getPeers() {
        return peers;
    }

    public Peers.Peer getSelf() {
        return peers.getSelf();
    }

    public RaftRpcClient getClient() {
        return raftRpcClient;
    }

    private class ElectionTask implements Runnable {
        @Override
        public void run() {
            if (state == ServerState.LEADER) {
                return;
            }
            // TODO: random timeout

            state = ServerState.CANDIDATE;
            currentTerm++;
            votedFor = getSelf().getAddr();
            ArrayList<Future<ReqVoteResp>> futureReqVoteResp = new ArrayList<>();
            for (Peers.Peer peer : peers.getPeerList()) {
                if (!peer.equals(peers.getSelf())) {
                    futureReqVoteResp.add(RaftThreadPool.submit(() -> {
                        int lastTerm = 0;
                        LogEntry lastLogEntry = logModule.back();
                        if (lastLogEntry != null) {
                            lastTerm = lastLogEntry.getTerm();
                        }
                        ReqVoteReq req = new ReqVoteReq(currentTerm, peer.getAddr(), getSelf().getAddr(), logModule.size() - 1, logModule.back().getTerm());
                        Object response = raftRpcClient.requestVoteRpc(req);
                        return (ReqVoteResp) response;
                    }));
                }
            }

            AtomicInteger success = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(futureReqVoteResp.size());
            for (Future<ReqVoteResp> future : futureReqVoteResp) {
                RaftThreadPool.submit(() -> {
                    try {
                        ReqVoteResp reqVoteResp = future.get(3000, TimeUnit.MILLISECONDS);
                        if (reqVoteResp == null) {
                            return -1;
                        }
                        if (reqVoteResp.isVoteGranted()) {
                            success.incrementAndGet();
                        } else {
                            int respTerm = reqVoteResp.getTerm();
                            if (respTerm > currentTerm) {
                                currentTerm = respTerm;
                            }
                        }
                    } catch (InterruptedException e) {
                        logger.error("Future.get(): ElectionTask interrupted", e);
                    } catch (ExecutionException e) {
                        logger.error("Future.get(): ElectionTask execution exception", e);
                    } catch (TimeoutException e) {
                        logger.error("Future.get(): ElectionTask timeout exception", e);
                    } finally {
                        latch.countDown();
                    }
                    return 0;
                });
            }

            try {
                latch.await(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("latch.await(): ElectionTask interrupted", e);
            }

            if (state == ServerState.FOLLOWER) {
                return;
            }

            if (success.get() >= peers.size() / 2) {
                logger.info("ElectionTask: node {} become leader", getSelf());
                state = ServerState.LEADER;
                nextIndex = new ConcurrentHashMap<>();
                matchIndex = new ConcurrentHashMap<>();
                for (Peers.Peer peer : peers.getPeerList()) {
                    if (peer != peers.getSelf()) {
                        nextIndex.put(peer, logModule.size());
                        matchIndex.put(peer, 0);
                    }
                }
            }
            votedFor = "";
        }
    }

    public void startElection() {
        electionTask.run();
    }
}

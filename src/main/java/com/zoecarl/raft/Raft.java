package com.zoecarl.raft;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.ConcurrentHashMap;

import com.zoecarl.common.LogEntry;
import com.zoecarl.common.Peers;
import com.zoecarl.common.Peers.Peer;
import com.zoecarl.concurr.RaftThreadPool;
import com.zoecarl.raft.raftrpc.RaftRpcClient;
import com.zoecarl.raft.raftrpc.RaftRpcServer;
import com.zoecarl.raft.raftrpc.common.AppendEntriesReq;
import com.zoecarl.raft.raftrpc.common.AppendEntriesResp;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteResp;
import com.zoecarl.utils.FileOp;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Raft {

    public static final Logger logger = LogManager.getLogger(Raft.class);

    public enum ServerState {
        FOLLOWER,
        CANDIDATE,
        LEADER
    }

    private ServerState state;
    private Peers peers;
    private int currentTerm;
    private String votedFor = "";
    private LogModule logModule;

    private int port;
    private String host;

    private RaftRpcClient raftRpcClient;
    private RaftRpcServer raftRpcServer;
    private ClusterManager clusterManager;


    private ElectionTask electionTask = new ElectionTask();
    private HeartBeatTask heartBeatTask = new HeartBeatTask();

    ConcurrentHashMap<Peer, Integer> nextIndex;
    ConcurrentHashMap<Peer, Integer> matchIndex;

    private StateMachine stateMachine;

    private final long heartBeatTick = 5000;
    public volatile long preHeartBeatTime = 0;
    private volatile long electionTime = 15000;
    public volatile long preElectionTime = 0;

    public Raft() {
        this.state = ServerState.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = "";
    }

    public Raft(String host, int port) {
        this.state = ServerState.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = "";
        this.host = host;
        this.port = port;
    }

    public void init(boolean launchServer) {
        raftRpcServer = new RaftRpcServer(port, this);
        raftRpcClient = new RaftRpcClient(host, port + 1);
        clusterManager = new ClusterManager(this);
        stateMachine = new StateMachine(port);
        String dbDir = "./rocksDB-raft/" + port + "/logModule";
        String logsDir = dbDir + "/logModule";
        logModule = new LogModule(dbDir, logsDir);
        peers = new Peers();
        peers.setSelf(host, port);
        if (launchServer == true) {
            raftRpcServer.start();
        }
    }

    public void init(int nodeId, String filename) {
        peers = new Peers();
        String settings = FileOp.readFile(filename);
        peers.loadSettings(nodeId, settings);
        String selfAddr[] = getSelf().getAddr().split(":");
        this.host = selfAddr[0];
        this.port = Integer.parseInt(selfAddr[1]);
        String dbDir = "./rocksDB-raft/" + port;
        String logsDir = dbDir + "/logModule";
        logModule = new LogModule(dbDir, logsDir);
        raftRpcServer = new RaftRpcServer(port, this);
        raftRpcClient = new RaftRpcClient(host, port + 1);
        clusterManager = new ClusterManager(this);
        stateMachine = new StateMachine(port);
        RaftThreadPool.scheduleAtFixedRate(electionTask, 5000, 15000);
        RaftThreadPool.scheduleWithFixedDelay(heartBeatTask, 5000);
        raftRpcServer.start();
    }

    public void init() {
        init(true);
        logger.info("raft node {} initialization successful", getSelfId());
    }

    public void addPeer(Peer peer) {
        clusterManager.addPeer(peer);
    }

    public void addPeer(String host, int port) {
        clusterManager.addPeer(host, port);
    }

    public void removePeer(Peer peer) {
        clusterManager.removePeer(peer);
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public void setCurrTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public void setLeader(Peer leader) {
        peers.setLeader(leader);
    }

    public void setLeader(String leaderId) {
        String host = leaderId.split(":")[0];
        int port = Integer.parseInt(leaderId.split(":")[1]);
        for (Peer peer : getPeers().getPeerList()) {
            if (peer.getAddr().equals(host + ":" + port)) {
                peers.setLeader(peer);
                break;
            }
        }
    }

    public Peers getPeers() {
        return peers;
    }

    public Peer getSelf() {
        return peers.getSelf();
    }

    public RaftRpcClient getClient() {
        return raftRpcClient;
    }

    public int getCurrTerm() {
        return currentTerm;
    }

    public String getSelfId() {
        return host + ":" + port;
    }

    public String getLeaderId() {
        return peers.getLeader().getAddr();
    }

    public String getVotedFor() {
        return votedFor;
    }

    public LogModule getLogModule() {
        return logModule;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public ServerState state() {
        return state;
    }

    private class ElectionTask implements Runnable {
        @Override
        public void run() {
            if (state == ServerState.LEADER) {
                return;
            }
            
            long current = System.currentTimeMillis();
            electionTime = electionTime + ThreadLocalRandom.current().nextInt(10000);
            if (current - preElectionTime < electionTime) {
                return;
            } else {
                preElectionTime = current;
            }

            logger.warn("{} starts a election task", getSelfId());
            logger.info("{} peers recorded", peers.getPeerList().size());
            
            state = ServerState.CANDIDATE;
            currentTerm++;
            logger.info("{} receive a self vote", getSelfId());
            votedFor = getSelf().getAddr();

            ArrayList<Future<ReqVoteResp>> futureReqVoteResp = new ArrayList<>();
            for (Peer peer : peers.getPeerList()) {
                if (!peer.equals(peers.getSelf())) {
                    // TODO: thread error
                    logger.info("{} prepare the task sending a vote request to {}", getSelfId(), peer.getAddr());
                    futureReqVoteResp.add(RaftThreadPool.submit(() -> {
                        LogEntry lastLogEntry = logModule.back();
                        int lastEntryTerm = lastLogEntry == null ? 0 : lastLogEntry.getTerm();
                        try {
                            ReqVoteReq req = new ReqVoteReq(currentTerm, peer.getAddr(), getSelf().getAddr(),
                                    logModule.size() - 1, lastEntryTerm);
                            // System.out.println(req);
                            Object response = raftRpcClient.requestVoteRpc(req);
                            return (ReqVoteResp) response;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }));
                }
            }

            // RaftThreadPool.shutdown();

            logger.info("requests sent, espected {} responses",
                    futureReqVoteResp.size());

            AtomicInteger success = new AtomicInteger(1);
            CountDownLatch latch = new CountDownLatch(futureReqVoteResp.size());
            for (Future<ReqVoteResp> future : futureReqVoteResp) {
                RaftThreadPool.submit(() -> {
                    try {
                        ReqVoteResp reqVoteResp = future.get(3000, TimeUnit.MILLISECONDS);
                        if (reqVoteResp == null) {
                            logger.error("{} get null response", getSelfId());
                            return 0;
                        }
                        if (reqVoteResp.isVoteGranted()) {
                            logger.info("{} receive a vote", getSelfId());
                            success.incrementAndGet();
                        } else {
                            int respTerm = reqVoteResp.getTerm();
                            if (respTerm > currentTerm) {
                                currentTerm = respTerm;
                            }
                        }
                        return 0;
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

            if (success.get() > peers.size() / 2) {
                logger.warn("ElectionTask: node {} become leader", getSelf());
                state = ServerState.LEADER;
                nextIndex = new ConcurrentHashMap<>();
                matchIndex = new ConcurrentHashMap<>();
                for (Peer peer : peers.getPeerList()) {
                    if (!peer.equals(peers.getSelf())) {
                        nextIndex.put(peer, logModule.size());
                        matchIndex.put(peer, 0);
                    }
                }
            } else {
                logger.warn("ElectionTask failed: only {} votes get", success.get());
            }
            votedFor = "";
        }
    }

    public void startElection() {
        electionTask.run();
    }

    class HeartBeatTask implements Runnable {
        @Override
        public void run() {
            if (state != ServerState.LEADER) {
                return;
            }
            logger.info("{} starts a heart beat task", getSelfId());
            long current = System.currentTimeMillis();
            if (current - preHeartBeatTime < heartBeatTick) {
                return;
            }

            preHeartBeatTime = System.currentTimeMillis();
            for (Peer peer : peers.getPeerList()) {
                if (peer.equals(peers.getSelf())) {
                    continue;
                }
                AppendEntriesReq req = new AppendEntriesReq(getCurrTerm(), peer.getAddr());
                RaftThreadPool.execute(() -> {
                    try {
                        AppendEntriesResp resp = raftRpcClient.appendEntriesRpc(req);
                        int term = resp.getTerm();
                        if (term > currentTerm) {
                            logger.warn("become a follower after receiving a higher term {}", term);
                            currentTerm = term;
                            votedFor = "";
                            state = ServerState.FOLLOWER;
                        }
                    } catch (Exception e) {
                        logger.error("append entries rpc failed at {}: {}", peer.getAddr(), e);
                    }
                }, false);
            }
        }
    }
}

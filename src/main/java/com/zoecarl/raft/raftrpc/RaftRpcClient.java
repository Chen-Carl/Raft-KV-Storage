package com.zoecarl.raft.raftrpc;

import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.rpc.RpcClient;
import com.zoecarl.raft.raftrpc.common.Request;
import com.zoecarl.raft.raftrpc.common.AddPeerReq;
import com.zoecarl.raft.raftrpc.common.AppendEntriesReq;
import com.zoecarl.raft.raftrpc.common.AppendEntriesResp;
import com.zoecarl.raft.raftrpc.common.RemovePeerReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.service.ReqVoteService;
import com.zoecarl.raft.raftrpc.service.SayHelloService;
import com.zoecarl.raft.raftrpc.service.AddPeerService;
import com.zoecarl.raft.raftrpc.service.AppendEntriesService;
import com.zoecarl.raft.Raft;

public class RaftRpcClient extends RpcClient {
    private static final Logger logger = LogManager.getLogger(RaftRpcClient.class);

    public RaftRpcClient(String host, int port) {
        super(host, port);
    }

    public String sayHelloRpc(String msg) {
        resetAddr("127.0.0.1", 13302);
        Class<SayHelloService> serviceClass = SayHelloService.class;
        try {
            Method method = serviceClass.getMethod("sayHello", String.class, Raft.class);
            Object[] args = {msg};
            Object obj = callRemoteProcedure(method, args, 1000, 1);
            return (String) obj;
        } catch (Exception e) {
            logger.error("call remote procedure error", e);
        }
        return null;
    }

    public Object requestVoteRpc(ReqVoteReq req) {
        String host = req.getHostname();
        int port = req.getPort();
        resetAddr(host, port);
        Class<ReqVoteService> serviceClass = ReqVoteService.class;
        try {
            Method method = serviceClass.getMethod("handleRequestVote", ReqVoteReq.class, Raft.class);
            Object[] arguments = { req };
            logger.info("{} send a request vote request to {}", req.getCandidateId(), host + ":" + port);
            Object obj = callRemoteProcedure(method, arguments, 1000, 1);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AppendEntriesResp appendEntriesRpc(AppendEntriesReq req) {
        String host = req.getHostname();
        int port = req.getPort();
        resetAddr(host, port);
        Class<AppendEntriesService> serviceClass = AppendEntriesService.class;
        // handleAppendEntries(AppendEntriesReq req, Raft selfNode)
        try {
            Method method = serviceClass.getMethod("handleAppendEntries", AppendEntriesReq.class, Raft.class);
            Object[] arguments = { req };
            if (req.getEntries() == null) {
                logger.info("LEADER send a heartbeat to {} with term={}", host + ":" + port, req.getTerm());
            } else {
                logger.info("{} send a append entries request to {}", req.getLeaderId(), host + ":" + port);
            }
            Object obj = callRemoteProcedure(method, arguments, 1000, 1);
            return (AppendEntriesResp) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addPeerRpc(AddPeerReq req) {
        String host = req.getHostname();
        int port = req.getPort();
        resetAddr(host, port);
        Class<AddPeerService> serviceClass = AddPeerService.class;
        try {
            Method method = serviceClass.getMethod("handleAddPeer", AddPeerReq.class, Raft.class);
            Object[] arguments = { req };
            logger.warn("{} send a add peer request to {}", getSelfAddr(), host + ":" + port);
            callRemoteProcedure(method, arguments, 1000, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePeerRpc(RemovePeerReq req) {
        String host = req.getHostname();
        int port = req.getPort();
        resetAddr(host, port);
        Class<AddPeerService> serviceClass = AddPeerService.class;
        try {
            Method method = serviceClass.getMethod("handleRemovePeer", Request.class, Raft.class);
            Object[] arguments = { req };
            callRemoteProcedure(method, arguments, 1000, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

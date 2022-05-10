package com.zoecarl.raft.raftrpc;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.rpc.RpcClient;
import com.zoecarl.utils.FileOp;
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
import com.zoecarl.raft.raftrpc.service.ClientKVService;
import com.zoecarl.common.ClientKVReq;
import com.zoecarl.common.ClientKVResp;
import com.zoecarl.common.ClientKVReq.Type;
import com.zoecarl.raft.Raft;

public class RaftRpcClient extends RpcClient {
    private static final Logger logger = LogManager.getLogger(RaftRpcClient.class);

    private HashMap<String, Integer> serverList;    // used only for users
    
    public void initKVService(String filename) {
        serverList = new HashMap<>();
        String settings = FileOp.readFile(filename);
        String[] lines = settings.split("\n");
        for (String line : lines) {
            String[] tokens = line.split(" ");
            serverList.put(tokens[0], Integer.parseInt(tokens[1]));
        }
    }

    // Respond to RPCs from candidates and leaders
    public RaftRpcClient(String host, int port) {
        super(host, port);
    }

    public RaftRpcClient(String host, int port, boolean user) {
        super(host, port);
        if (user) {
            initKVService("settings.txt");
        }
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

    // usr rpc
    public boolean put(String key, String value) {
        HashMap.Entry<?, ?> entry = serverList.entrySet().iterator().next();
        String host = (String) entry.getKey();
        int port = (int) entry.getValue();
        resetAddr(host, port);
        ClientKVReq req = new ClientKVReq(key, value, Type.PUT);
        Class<ClientKVService> serviceClass = ClientKVService.class;
        try {
            Method method = serviceClass.getMethod("handleClientKVReq", ClientKVReq.class, Raft.class);
            Object[] arguments = { req };
            ClientKVResp success = (ClientKVResp) callRemoteProcedure(method, arguments, 1000, 1);
            return (success.getValue() == "true" ? true : false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String get(String key) {
        HashMap.Entry<?, ?> entry = serverList.entrySet().iterator().next();
        String host = (String) entry.getKey();
        int port = (int) entry.getValue();
        resetAddr(host, port);
        ClientKVReq req = new ClientKVReq(key, null, Type.GET);
        Class<ClientKVService> serviceClass = ClientKVService.class;
        try {
            Method method = serviceClass.getMethod("handleClientKVReq", ClientKVReq.class, Raft.class);
            Object[] arguments = { req };
            ClientKVResp success = (ClientKVResp) callRemoteProcedure(method, arguments, 1000, 1);
            return success.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

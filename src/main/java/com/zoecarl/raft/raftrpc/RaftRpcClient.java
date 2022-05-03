package com.zoecarl.raft.raftrpc;

import java.lang.reflect.Method;

import com.zoecarl.rpc.RpcClient;
import com.zoecarl.raft.raftrpc.common.Request;
import com.zoecarl.raft.raftrpc.common.Response;
import com.zoecarl.raft.raftrpc.common.AddPeerReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.service.ReqVoteService;
import com.zoecarl.raft.raftrpc.service.AddPeerService;
import com.zoecarl.raft.Raft;

public class RaftRpcClient extends RpcClient {
    public RaftRpcClient(String host, int port) {
        super(host, port);
    }

    public Object requestVoteRpc(ReqVoteReq req) {
        String host = req.getHostname();
        int port = req.getPort();
        resetConnection(host, port);
        Class<ReqVoteService> serviceClass = ReqVoteService.class;
        try {
            Method method = serviceClass.getMethod("handleRequestVote", Request.class, Raft.class);
            Object[] arguments = { req };
            Object obj = callRemoteProcedure(method, arguments, 1000, 1);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response appendEntriesRpc() {
        return null;
    }

    public void addPeerRpc(AddPeerReq req) {
        String host = req.getHostname();
        int port = req.getPort();
        resetConnection(host, port);
        Class<AddPeerService> serviceClass = AddPeerService.class;
        try {
            Method method = serviceClass.getMethod("handleAddPeer", Request.class);
            Object[] arguments = { req };
            callRemoteProcedure(method, arguments, 1000, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

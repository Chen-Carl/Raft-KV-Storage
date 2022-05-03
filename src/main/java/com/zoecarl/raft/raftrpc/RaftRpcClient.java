package com.zoecarl.raft.raftrpc;

import java.lang.reflect.Method;

import com.zoecarl.raft.raftrpc.service.RequestVoteService;
import com.zoecarl.rpc.RpcClient;
import com.zoecarl.common.ReqVoteArgs;

public class RaftRpcClient extends RpcClient {
    public RaftRpcClient(String host, int port) {
        super(host, port);
    }

    public Response requestVoteRpc(Request req) {
        String host = ((ReqVoteArgs) req.getReqArgs()).getHostname();
        int port = ((ReqVoteArgs) req.getReqArgs()).getPort();
        resetConnection(host, port);
        Class<RequestVoteService> serviceClass = RequestVoteService.class;
        try {
            Method method = serviceClass.getMethod("handleRequestVote", Request.class);
            Object[] arguments = { req };
            Object obj = callRemoteProcedure(method, arguments, 1000, 1);
            return (Response) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response appendEntriesRpc() {
        return null;
    }
}

package com.zoecarl.raft.raftrpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;

import com.zoecarl.raft.Raft;
import com.zoecarl.raft.raftrpc.service.AddPeerService;
import com.zoecarl.raft.raftrpc.service.AppendEntriesService;
import com.zoecarl.raft.raftrpc.service.ReqVoteService;
import com.zoecarl.rpc.RpcServer;

public class RaftRpcServer extends RpcServer {
    Raft selfNode;

    public RaftRpcServer(int port, Raft raft) {
        super(port);
        this.selfNode = raft;
        super.register(ReqVoteService.class);
        super.register(AppendEntriesService.class);
        super.register(AddPeerService.class);
    }

    @Override
    protected void handleRPCRequest(Socket socket) {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            String serviceClassName = ois.readUTF();
            String methodName = ois.readUTF();
            Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
            Object[] arguments = (Object[]) ois.readObject();
            Object[] newObjArr = new Object[arguments.length + 1];
            for (int i = 0; i < arguments.length; i++) {
                newObjArr[i] = arguments[i];
            }
            newObjArr[arguments.length] = selfNode;
            arguments = newObjArr;
            logger.info("receive rpc request, interfaceName: {}, methodName: {}, parameterTypes: {}, arguments: {}",
                    serviceClassName, methodName, parameterTypes, arguments);
            Object rt = serviceManager.executeService(serviceClassName, methodName, parameterTypes, arguments);
            logger.info("execution successful");
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(rt);
            logger.info("result sent to client...");
            socket.close();
            oos.close();
            ois.close();
        } catch (IOException e) {
            logger.error("handle rpc request error", e);
        } catch (ClassNotFoundException e) {
            logger.error("read object error: ", e);
        }
    }
}

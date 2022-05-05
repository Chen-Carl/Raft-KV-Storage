package com.zoecarl.raft.raftrpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.raft.Raft;
import com.zoecarl.raft.raftrpc.service.AddPeerService;
import com.zoecarl.raft.raftrpc.service.AppendEntriesService;
import com.zoecarl.raft.raftrpc.service.ReqVoteService;
import com.zoecarl.raft.raftrpc.service.SayHelloService;
import com.zoecarl.rpc.RpcServer;

public class RaftRpcServer extends RpcServer implements Runnable {
    private Thread t;
    private Raft selfNode;

    public RaftRpcServer(int port, Raft raft) {
        super(port);
        this.selfNode = raft;
        super.register(ReqVoteService.class);
        super.register(AppendEntriesService.class);
        super.register(AddPeerService.class);
        super.register(SayHelloService.class);
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
            logger.info("\nreceive rpc request: \n\tinterfaceName: {}\n\tmethodName: {}\n\targuments: {}",
                    serviceClassName, methodName,  arguments);
            Object rt = serviceManager.executeService(serviceClassName, methodName, parameterTypes, arguments);
            logger.info("{} execution successful", methodName);
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

    @Override
    public void run() {
        launch();
    }

    public void start() {
        System.out.println("Starting raft server...");
        if (t == null) {
            t = new Thread(this, "raft server");
            t.start();
        }
    }
}

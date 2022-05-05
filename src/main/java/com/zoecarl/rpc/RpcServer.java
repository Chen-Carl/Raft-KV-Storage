package com.zoecarl.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RpcServer {
    public static final int POOL_SIZE = 2 * Runtime.getRuntime().availableProcessors();

    protected static final Logger logger = LogManager.getLogger(RpcServer.class);
    private ExecutorService executorService = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
    protected ServiceManager serviceManager = new ServiceManager();
    private ServerSocket serverSocket;

    public RpcServer(ExecutorService executorService, ServiceManager serviceManager, ServerSocket serverSocket) {
        this.executorService = executorService;
        this.serviceManager = serviceManager;
        this.serverSocket = serverSocket;
    }

    public RpcServer(ExecutorService executorService, int port) {
        this.executorService = executorService;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            logger.error("create server socket error", e);
        }
    }

    public RpcServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            logger.error("create server socket error", e);
        }
    }

    public void launch() {
        logger.info("server running at {}", serverSocket);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("accept a new connection from {}", socket);
                executorService.execute(() -> {
                    try {
                        handleRPCRequest(socket);
                    } catch (Exception e) {
                        logger.error("handle rpc request error", e);
                    }
                });
            } catch (Exception e) {
                logger.error("accept a new connection error", e);
            }
        }
    }

    protected void handleRPCRequest(Socket socket) {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            String serviceClassName = ois.readUTF();
            String methodName = ois.readUTF();
            Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
            Object[] arguments = (Object[]) ois.readObject();
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

    public void register(Class<?> serviceClass) {
        serviceManager.register(serviceClass);
    }

    public void unregister(Class<?> serviceClass) {
        serviceManager.unregister(serviceClass);
    }
}

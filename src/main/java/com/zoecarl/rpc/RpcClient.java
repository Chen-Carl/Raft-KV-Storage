package com.zoecarl.rpc;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RpcClient {
    private static final Logger logger = LogManager.getLogger(RpcClient.class);

    private String host;
    private int port;

    private SocketAddress address;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.address = new InetSocketAddress(host, port);
    }

    protected String getSelfAddr() {
        return host + ":" + port;
    }

    public void resetAddr(String host, int port) {
        this.address = new InetSocketAddress(host, port);
    }

    public Object callRemoteProcedure(Method method, Object[] arguments, int timeout, int retries) {
        Socket socket = new Socket();
        Object res = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        boolean needRetry = true;
        try {
            retries++;
            while (retries-- > 0 && needRetry) {
                socket.connect(address, timeout);
                socket.setSoTimeout(timeout);
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeUTF(method.getDeclaringClass().getName());
                oos.writeUTF(method.getName());
                oos.writeObject(method.getParameterTypes());
                oos.writeObject(arguments);
                ois = new ObjectInputStream(socket.getInputStream());
                res = ois.readObject();
                needRetry = false;

                socket.close();
                oos.close();
                ois.close();
            }
        } catch (InterruptedIOException e) {
            needRetry = true;
        } catch (IOException e) {
            logger.error("call remote procedure error", e);
        } catch (ClassNotFoundException e) {
            logger.error("call remote procedure error: readObject error: ", e);
        }
        if (res instanceof Exception) {
            throw new RuntimeException((Exception) res);
        }
        return res;
    }
}

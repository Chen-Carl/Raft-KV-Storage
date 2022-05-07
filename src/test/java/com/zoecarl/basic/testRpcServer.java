package com.zoecarl.basic;

import com.zoecarl.rpc.RpcServer;
import com.zoecarl.rpc.SayHello;

public class testRpcServer {
    public static void main(String[] args) {
        RpcServer server = new RpcServer(13308);
        server.register(SayHello.class);
        server.launch();
    }
}

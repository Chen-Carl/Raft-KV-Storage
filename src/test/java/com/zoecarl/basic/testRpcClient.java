package com.zoecarl.basic;

import java.lang.reflect.Method;

import com.zoecarl.rpc.RpcClient;
import com.zoecarl.rpc.SayHello;

public class testRpcClient {
    public static void main(String[] args) {
        RpcClient client = new RpcClient("127.0.0.1", 13308);
        Class<SayHello> serviceClass = SayHello.class;
        try {
            Method method = serviceClass.getMethod("sayHello", String.class);
            Object[] arguments = {"world"};
            Object obj = client.callRemoteProcedure(method, arguments, 1000, 1);
            System.out.println(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

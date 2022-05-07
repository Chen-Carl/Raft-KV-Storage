package com.zoecarl.basic;

import com.zoecarl.concurr.RaftThreadPool;
import com.zoecarl.concurr.RaftThreadPoolExecutor;

import com.zoecarl.rpc.RpcClient;
import com.zoecarl.rpc.SayHello;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.Future;

public class testThreadPool {
    public static void main(String[] args) {
        RaftThreadPoolExecutor executor = RaftThreadPool.getThreadPoolExecutor();
        ArrayList<Future<String>> future = new ArrayList<>();
        
        future.add(executor.submit(() -> {
            RpcClient client = new RpcClient("127.0.0.1", 13308);
            Class<SayHello> serviceClass = SayHello.class;
            try {
                Method method = serviceClass.getMethod("sayHello", String.class);
                Object[] arguments = {"world"};
                Object obj = client.callRemoteProcedure(method, arguments, 1000, 1);
                System.out.println(obj);
                return (String) obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }));
        executor.shutdown();
        for (Future<String> f : future) {
            try {
                System.out.println(f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package com.zoecarl.raft.raftrpc.service;

import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.raft.Raft;

public class SayHelloService implements ServiceProvider {
    public String sayHello(String name, Raft selfNode) {
        return "hello, " + name;
    }
}

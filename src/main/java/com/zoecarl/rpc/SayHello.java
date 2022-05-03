package com.zoecarl.rpc;

public class SayHello implements ServiceProvider {
    public String sayHello(String name) {
        return "hello, " + name;
    }
}

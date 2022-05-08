package com.zoecarl.basic;

import com.zoecarl.common.Peers;
import com.zoecarl.common.Peers.Peer;

public class testPeerEquel {
    public static void main(String[] args) {
        Peers obj = new Peers();
        Peer p1 = obj.new Peer("127.0.0.1:8000");
        Peer p2 = obj.new Peer("127.0.0.1:8000");
        if (p1.equals(p2)) {
            System.out.println("equal");
        } else {
            System.out.println("not equal");
        }
    }
}
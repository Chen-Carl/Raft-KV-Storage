package com.zoecll;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.zoecll.config.PeerInfo;
import com.zoecll.raftrpc.RaftNode;

public class App 
{
    public static void main(String[] args) {
        try {
            ArrayList<PeerInfo> peerInfos = PeerInfo.loadConfig();
            for (int i = 0; i < peerInfos.size(); i++) {
                final int finalId = i;
                new Thread(() -> {
                    RaftNode raftNode = new RaftNode(finalId, peerInfos);
                    raftNode.start();
                }).start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

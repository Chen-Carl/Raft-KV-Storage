package com.zoecll.raftrpc;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Test;

import com.zoecll.config.PeerInfo;

public class ElectionTask {

    // private final static Logger logger = LoggerFactory.getLogger(ElectionTask.class);
    
    @Test
    public void testElectionTask() {
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

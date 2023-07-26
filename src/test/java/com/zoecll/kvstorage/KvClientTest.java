package com.zoecll.kvstorage;

import java.util.ArrayList;
import org.junit.Test;
import com.zoecll.config.PeerInfo;

public class KvClientTest {

    KvClient kvClient;

    public KvClientTest() {
        try {
            ArrayList<PeerInfo> peers = PeerInfo.loadConfig();
            kvClient = new KvClient(peers);
        } catch (Exception e) {
            e.printStackTrace();
        };
    }

    @Test
    public void testGet() {
        String res = kvClient.getNoRetry("test");
        System.out.println(res);
    }

    @Test
    public void testSet() {
        kvClient.setNoRetry("test", "test info");
    }

}

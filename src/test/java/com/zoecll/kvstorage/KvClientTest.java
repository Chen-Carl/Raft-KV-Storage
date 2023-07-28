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
    public void testGetNoRetry() {
        String res = kvClient.getNoRetry("test");
        System.out.println(res);
    }

    @Test
    public void testSetNoRetry() {
        kvClient.setNoRetry("test", "test info");
    }

    @Test
    public void testGet() {
        for (int i = 0; i < 100; i++) {
            String res = kvClient.get("key-" + i);
            System.out.println(res);
        }
    }

    @Test
    public void testSet() {
        for (int i = 0; i < 100; i++) {
            kvClient.set("key-" + i, "value-" + i);
        }
    }

}

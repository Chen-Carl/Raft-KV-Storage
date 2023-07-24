package com.zoecll;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.zoecll.raftrpc.PeerInfo;
import com.zoecll.raftrpc.RaftNode;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Hello world!
 *
 */
public class App 
{

    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        try {
            InputStream input = new FileInputStream("src/main/resources/config.yml");
            Map<String, Map<String, List<Map<String, Object>>>> data = yaml.load(input);
            List<Map<String, Object>> nodes = data.get("raft").get("nodes");

            ArrayList<PeerInfo> peerInfos = new ArrayList<>();
            for (Map<String, Object> node : nodes) {
                String host = (String) node.get("address");
                int port = (int) node.get("port");
                peerInfos.add(new PeerInfo(host, port));
            }

            for (Map<String, Object> node : nodes) {
                int id = (int) node.get("id");
                int port = (int) node.get("port");
                new Thread(() -> {
                    Server server = ServerBuilder.forPort(port).addService(new RaftNode(id, peerInfos)).build();
                    try {
                        server.start();
                        server.awaitTermination();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            
        } catch (FileNotFoundException e) {
            logger.error("Node config file not found.");
            e.printStackTrace();
        }
    }
}

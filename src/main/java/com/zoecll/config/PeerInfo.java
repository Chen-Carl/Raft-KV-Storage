package com.zoecll.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;

@Getter
public class PeerInfo {
    private int id;
    private ManagedChannel rpcChannel;
    private ManagedChannel kvChannel;
    private String host;
    private int rpcPort;
    private int kvPort;

    public PeerInfo(int id, String host, int rpcPort, int kvPort) {
        this.id = id;
        this.host = host;
        this.rpcPort = rpcPort;
        this.kvPort = kvPort;
        this.rpcChannel = ManagedChannelBuilder.forAddress(host, rpcPort).usePlaintext().build();
        this.kvChannel = ManagedChannelBuilder.forAddress(host, kvPort).usePlaintext().build();
    }

    public static ArrayList<PeerInfo> loadConfig() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream input = new FileInputStream("src/main/resources/config.yml");
        Map<String, Map<String, List<Map<String, Object>>>> data = yaml.load(input);
        List<Map<String, Object>> nodes = data.get("cluster").get("nodes");

        ArrayList<PeerInfo> peerInfos = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            int id = (int) node.get("id");
            String host = (String) node.get("address");
            int rpcPort = (int) node.get("rpcPort");
            int kvPort = (int) node.get("kvPort");
            peerInfos.add(new PeerInfo(id, host, rpcPort, kvPort));
        }

        return peerInfos;
    }
}

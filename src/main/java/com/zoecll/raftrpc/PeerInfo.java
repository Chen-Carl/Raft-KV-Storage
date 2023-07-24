package com.zoecll.raftrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PeerInfo {
    private ManagedChannel channel;

    public PeerInfo(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    ManagedChannel getChannel() {
        return channel;
    }
}

package com.zoecarl.raft;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.common.Peers;
import com.zoecarl.common.Peers.Peer;
import com.zoecarl.raft.raftrpc.common.AddPeerReq;
import com.zoecarl.raft.raftrpc.common.RemovePeerReq;

public class ClusterManager {
    private static final Logger logger = LogManager.getLogger(ClusterManager.class);

    private Raft selfNode;

    public ClusterManager(Raft self) {
        this.selfNode = self;
    }

    synchronized public void addPeer(Peer newPeer) {
        Peers peers = selfNode.getPeers();
        // TODO: replicate logs
        if (!peers.count(newPeer)) {
            peers.addPeer(newPeer);
        }

        // TODO: RPC add peer
        for (Peer peer : peers.getPeerList()) {
            if (!peer.equals(selfNode.getSelf())) {
                String hostname = peer.getAddr();
                AddPeerReq req = new AddPeerReq(hostname, newPeer);
                selfNode.getClient().addPeerRpc(req);
            }
        }
    }

    synchronized public void addPeer(String hostname, int port) {
        Peers peers = selfNode.getPeers();
        Peer peer = peers.new Peer(hostname, port);
        addPeer(peer);
    }

    synchronized public void removePeer(Peer oldPeer) {
        Peers peers = selfNode.getPeers();
        if (peers.count(oldPeer)) {
            peers.removePeer(oldPeer);
        }
        // TODO: RPC remove peer
        for (Peer peer : peers.getPeerList()) {
            if (!peer.equals(selfNode.getSelf())) {
                String hostname = peer.getAddr();
                RemovePeerReq req = new RemovePeerReq(hostname, peer);
                selfNode.getClient().removePeerRpc(req);
            }
        }
    }
}

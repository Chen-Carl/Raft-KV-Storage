package com.zoecarl.raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoecarl.common.Peers;
import com.zoecarl.common.Peers.Peer;
import com.zoecarl.raft.raftrpc.common.AddPeerReq;

public class ClusterManager {
    private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

    private Raft selfNode;

    synchronized public void addPeer(Peer newPeer) {
        Peers peers = selfNode.getPeers();
        if (!peers.count(newPeer)) {
            peers.addPeer(newPeer);
        }
        // TODO: replicate logs

        // TODO: RPC add peer
        for (Peer peer : peers.getPeerList()) {
            if (peer != selfNode.getSelf()) {
                String hostname = peer.getAddr();
                AddPeerReq req = new AddPeerReq(hostname, newPeer);
                selfNode.getClient().addPeerRpc(req);
            }
        }
    }

    synchronized public void removePeer(Peer peer) {
        Peers peers = selfNode.getPeers();
        if (peers.count(peer)) {
            peers.removePeer(peer);
        }
        // TODO: RPC remove peer
    }
}

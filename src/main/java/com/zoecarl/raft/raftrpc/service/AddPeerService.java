package com.zoecarl.raft.raftrpc.service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.zoecarl.raft.raftrpc.common.AddPeerReq;
import com.zoecarl.rpc.ServiceProvider;
import com.zoecarl.raft.Raft;

public class AddPeerService implements ServiceProvider {
    private static final Logger logger = LogManager.getLogger("server");
    public void handleAddPeer(AddPeerReq req, Raft selfNode) {
        logger.warn("{} receive a add peer request to add {}", selfNode.getPeers().getSelf(), req.getPeerId());
    }
}

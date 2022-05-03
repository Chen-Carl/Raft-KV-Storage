package com.zoecarl.raft.raftrpc.common;

import com.zoecarl.common.Peers.Peer;

public class RemovePeerReq extends Request {
    private Peer oldPeer;

    public RemovePeerReq(String serverId, Peer oldPeer) {
        super(serverId);
        this.oldPeer = oldPeer;
    }

    public RemovePeerReq(String serverId, Peer oldPeer, Object content) {
        super(serverId, content);
        this.oldPeer = oldPeer;
    }
}

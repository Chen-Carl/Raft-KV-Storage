package com.zoecarl.raft.raftrpc.common;

import com.zoecarl.common.Peers.Peer;

public class AddPeerReq extends Request {
    private Peer newPeer;

    public AddPeerReq(String serverId, Peer newPeer) {
        super(serverId);
        this.newPeer = newPeer;
    }

    public AddPeerReq(String serverId, Peer newPeer, Object content) {
        super(serverId, content);
        this.newPeer = newPeer;
    }
}

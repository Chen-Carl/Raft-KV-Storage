package com.zoecarl.basic;

import com.zoecarl.common.Peers;
import com.zoecarl.common.Peers.Peer;
import com.zoecarl.raft.raftrpc.RaftRpcClient;
import com.zoecarl.raft.raftrpc.common.AddPeerReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteReq;
import com.zoecarl.raft.raftrpc.common.ReqVoteResp;

public class testRaftRpcClient {
    public static void main(String[] args) {
        RaftRpcClient client = new RaftRpcClient("127.0.0.1", 13308);
        // testReqVote(client);
        testAddPeer(client);
    }

    public static void testReqVote(RaftRpcClient client) {
        System.out.println("========== test request vote rpc ==========");
        ReqVoteReq req = new ReqVoteReq(1, "127.0.0.1:13308", "127.0.0.1:13308", 1, 1);
        ReqVoteResp resp = (ReqVoteResp) client.requestVoteRpc(req);
        System.out.println(resp.getContent());
    }

    public static void testAddPeer(RaftRpcClient client) {
        client.resetAddr("127.0.0.1", 13308);
        System.out.println("========== test add peer rpc ==========");
        Peers peers = new Peers();
        Peer newPeer = peers.new Peer("127.0.0.1", 13309);
        AddPeerReq req = new AddPeerReq("127.0.0.1:13308", newPeer);
        client.addPeerRpc(req);
    }
}

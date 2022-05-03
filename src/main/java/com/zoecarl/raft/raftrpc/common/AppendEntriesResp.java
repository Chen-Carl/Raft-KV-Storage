package com.zoecarl.raft.raftrpc.common;

public class AppendEntriesResp extends Response {
    public AppendEntriesResp() {
        super();
    }
    
    public AppendEntriesResp(Object content) {
        super(content);
    }
}

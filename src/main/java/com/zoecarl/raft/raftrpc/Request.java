package com.zoecarl.raft.raftrpc;

import java.io.Serializable;

public class Request implements Serializable {
    public enum RequestType {
        REQUEST_VOTE,
        APPEND_ENTRIES,
    };

    private RequestType type;
    private Object reqArgs;

    public Request(RequestType type, Object reqArgs) {
        this.type = type;
        this.reqArgs = reqArgs;
    }

    public Object getReqArgs() {
        return reqArgs;
    }
}

package com.zoecarl.raft.raftrpc;

import java.io.Serializable;

public class Response implements Serializable {
    public enum ResponseType {
        REQUEST_VOTE,
        APPEND_ENTRIES,
    };

    private ResponseType type;
    private Object resp;

    public Response(ResponseType type, Object resp) {
        this.type = type;
        this.resp = resp;
    }

    public Object getResp() {
        return resp;
    }

    public ResponseType getType() {
        return type;
    }
}

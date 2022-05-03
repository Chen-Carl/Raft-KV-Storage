package com.zoecarl.raft.raftrpc.common;

import java.io.Serializable;

public class Response implements Serializable {

    private Object content;

    public Response() {
        content = null;
    }

    public Response(Object resp) {
        this.content = resp;
    }

    public Object getContent() {
        return content;
    }
}

package com.zoecarl.raft.raftrpc.common;

import java.io.Serializable;

public class Request implements Serializable {
    private String serverId;
    private Object content;

    public Request(String serverId) {
        this.serverId = serverId;
        this.content = null;
    }

    public Request(String serverId, Object content) {
        this.serverId = serverId;
        this.content = content;
    }

    public String getHostname() {
        return serverId.split(":")[0];
    }

    public int getPort() {
        return Integer.parseInt(serverId.split(":")[1]);
    }

    public Object getReqArgs() {
        return content;
    }
}

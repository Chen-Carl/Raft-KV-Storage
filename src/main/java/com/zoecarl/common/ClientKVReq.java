package com.zoecarl.common;

import java.io.Serializable;

public class ClientKVReq implements Serializable {
    public enum Type {
        PUT, GET;
    };

    private String key;
    private String value;
    private Type type;

    public ClientKVReq(String key, String value, Type type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getTypeString() {
        if (type == Type.PUT) {
            return "PUT";
        } else if (type == Type.GET) {
            return "GET";
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

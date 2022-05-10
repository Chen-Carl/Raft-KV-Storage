package com.zoecarl.common;

public class ClientKVResp {
    private String key;
    private String value;

    public ClientKVResp() {
        this.key = null;
        this.value = null;
    }

    public ClientKVResp(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

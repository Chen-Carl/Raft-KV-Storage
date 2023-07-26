package com.zoecll.kvstorage;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KvCommand {
    enum Type { GET, SET };

    private Type type;
    private String key;
    private String value;
    private String uuid;

    @JsonCreator
    public KvCommand() {
        
    }
}
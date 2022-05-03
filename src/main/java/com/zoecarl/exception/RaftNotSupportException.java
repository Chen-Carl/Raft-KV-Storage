package com.zoecarl.exception;

public class RaftNotSupportException extends RuntimeException {
    public RaftNotSupportException(String message) {
        super(message);
    }
}
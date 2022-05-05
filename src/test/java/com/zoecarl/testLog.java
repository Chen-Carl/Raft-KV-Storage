package com.zoecarl;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

class TestLog {
    static final Logger log = LogManager.getLogger("raft");

    public static void main(String[] args) {
        log.info("hello, my name is {}", "zoe carl");
    }
}
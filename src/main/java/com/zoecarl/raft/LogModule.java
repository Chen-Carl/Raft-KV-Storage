package com.zoecarl.raft;

import com.zoecarl.common.LogEntry;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;

public class LogModule {
    private static final Logger logger = LoggerFactory.getLogger(LogModule.class);
    private final static byte[] LAST_INDEX_KEY = "LAST_INDEX_KEY".getBytes();
    private RocksDB logDB;

    LogEntry back() {
        try {
            byte[] last = logDB.get(LAST_INDEX_KEY);
            if (last == null) {
                last = "-1".getBytes();
            }
            byte[] res = logDB.get(last);
            if (res == null) {
                return null;
            }
            return JSON.parseObject(res, LogEntry.class);
        } catch (RocksDBException e) {
            logger.error("get last index error", e);
        }
        return null;
    }

    int size() {
        byte[] last = "-1".getBytes();
        try {
            last = logDB.get(LAST_INDEX_KEY);
            if (last == null) {
                last = "-1".getBytes();
            }
        } catch (RocksDBException e) {
            logger.error("get last index error", e);
        }
        return Integer.valueOf(new String(last)) + 1;
    }
}

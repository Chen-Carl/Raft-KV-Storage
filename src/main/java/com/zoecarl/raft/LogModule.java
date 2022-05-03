package com.zoecarl.raft;

import java.io.File;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;

import com.zoecarl.common.LogEntry;


public class LogModule {
    private static final Logger logger = LoggerFactory.getLogger(LogModule.class);
    private final static byte[] LAST_INDEX_KEY = "LAST_INDEX_KEY".getBytes();
    private RocksDB logDB;
    private String dbDir;
    private String logsDir;

    public LogModule(String dbDir, String logsDir) {
        this.dbDir = dbDir;
        this.logsDir = logsDir;
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);

        File file = new File(logsDir);
        boolean success = false;
        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            logger.warn("make a new dir : " + logsDir);
        }
        try {
            logDB = RocksDB.open(options, logsDir);
        } catch (RocksDBException e) {
            logger.error("cannot open rocksdb: ", e.getMessage());
        }
    }

    public LogEntry back() {
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

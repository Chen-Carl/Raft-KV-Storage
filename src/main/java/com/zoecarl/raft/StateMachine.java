package com.zoecarl.raft;

import java.io.File;

import com.alibaba.fastjson.JSON;
import com.zoecarl.common.LogEntry;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class StateMachine {
    private static final Logger logger = LogManager.getLogger(StateMachine.class);
    private RocksDB machineDB;

    public StateMachine(int port) {
        String dbDir = "./rocksDB-raft/" + port + "/stateMachine";
        String stateMachineDir = dbDir + "/stateMachine";
        RocksDB.loadLibrary();
        File file = new File(stateMachineDir);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            if (success) {
                logger.info("make a new dir : " + stateMachineDir);
            }
        }
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            machineDB = RocksDB.open(options, stateMachineDir);
        } catch (RocksDBException e) {
            System.out.println("cannot open rocksdb: " + e.getMessage());
        }
    }

    // private static class Instance {
    //     private static final StateMachine INSTANCE = new StateMachine(0);
    // }

    // public static StateMachine getInstance() {
    //     return Instance.INSTANCE;
    // }

    synchronized public void apply(LogEntry logEntry) {
        String key = logEntry.getKey();
        try {
            machineDB.put(key.getBytes(), JSON.toJSONBytes(logEntry));
        } catch (RocksDBException e) {
            logger.error("cannot put key {} to rocksdb: {}", key, e.getMessage());
        }
    }

    public LogEntry get(String key) {
        try {
            byte[] res = machineDB.get(key.getBytes());
            return JSON.parseObject(res, LogEntry.class);
        } catch (RocksDBException e) {
            logger.error("cannot get key {} from rocksdb: {}", key, e.getMessage());
        }
        return null;
    }

}

package com.zoecarl.raft;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.Options;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.alibaba.fastjson.JSON;

import com.zoecarl.common.LogEntry;

public class LogModule {
    private static final Logger logger = LogManager.getLogger(LogModule.class);
    private final static byte[] LAST_INDEX_KEY = "LAST_INDEX_KEY".getBytes();
    private RocksDB logDB;
    private String dbDir;
    private String logsDir;

    ReentrantLock lock = new ReentrantLock();

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
            logger.error("cannot open rocksdb: ", e);
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

    public int size() {
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

    public boolean empty() {
        return size() == 0;
    }

    public void write(LogEntry logEntry) {
        try {
            lock.tryLock(3000, TimeUnit.MILLISECONDS);
            Integer index = size();
            logEntry.setIndex(index);
            logDB.put(index.toString().getBytes(), JSON.toJSONBytes(logEntry));
            logger.info("write log entry: {}", logEntry);
            try {
                logDB.put(LAST_INDEX_KEY, index.toString().getBytes());
            } catch (RocksDBException e) {
                logger.error("RocksDB write: write last index error", e);
            }
        } catch (RocksDBException e) {
            logger.error("RocksDB write: write log entry error: ", e);
        } catch (InterruptedException e) {
            logger.error("write log entry error by interruption: ", e);
        } finally {
            lock.unlock();
        }
    }

    public LogEntry read(Integer index) {
        try {
            lock.tryLock(3000, TimeUnit.MILLISECONDS);
            byte[] res = logDB.get(index.toString().getBytes()); // RocksDBException
            if (res == null) {
                logger.error("empty log entry at index {}", index);
                return null;
            }
            LogEntry log = JSON.parseObject(res, LogEntry.class);
            logger.info("read log entry: {}", log);
        } catch (InterruptedException e) {
            logger.error("read log entry error by interruption: ", e);
        } catch (RocksDBException e) {
            logger.error("RocksDB read: read log entry error: ", e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void removeOnStartIndex(Integer startIndex) {
        try {
            lock.tryLock(3000, TimeUnit.MILLISECONDS);
            for (int i = startIndex; i < size(); i++) {
                logDB.delete(String.valueOf(i).getBytes());
            }
            logger.warn("rocksDB remove logs from start index {}, count = {}", startIndex, size() - startIndex);
            try {
                logDB.put(LAST_INDEX_KEY, ((Integer) (startIndex - 1)).toString().getBytes());
                logger.warn("last index = {}", startIndex - 1);
            } catch (RocksDBException e) {
                logger.error("RocksDB write: write last index error", e);
            }
        } catch (RocksDBException e) {
            logger.error("RocksDB remove: remove log entry error: ", e);
        } catch (InterruptedException e) {
            logger.error("remove log entry error by interruption: ", e);
        } finally {
            lock.unlock();
        }
    }
}

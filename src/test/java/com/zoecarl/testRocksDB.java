package com.zoecarl;

import com.zoecarl.raft.LogModule;
import com.alibaba.fastjson.JSON;
import com.zoecarl.common.LogEntry;

public class testRocksDB {
    public static void main(String[] args) {
        testBasicOperation();
        // testJSON();
    }

    public static void testBasicOperation() {
        LogModule logModule = new LogModule("./test/", "./test/data-cf/");
        if (logModule.empty()) {
            System.out.println("log module is empty");
            System.out.println("try to read back ...");
            LogEntry res = logModule.back();
            if (res == null) {
                System.out.println("back is null");
            } else {
                System.out.println(res);
            }
        }
        LogEntry logEntry = new LogEntry(0, "hello rocksdb");
        logModule.write(logEntry);
        if (!logModule.empty()) {
            for (int i = 0; i < logModule.size(); i++) {
                LogEntry res = logModule.read(i);
            }
        }
        logModule.removeOnStartIndex(0);
    }

    public static void testJSON() {
        System.out.println("========== test String to JSON ==========");
        String str = "hello rocksdb";
        byte[] strBytes = JSON.toJSONBytes(str);
        String strRes = JSON.parseObject(strBytes, String.class);
        if (str.equals(strRes)) {
            System.out.println("equal");
        } else {
            System.out.println("not equal");
        }

        System.out.println("========== test LogEntry ==========");
        LogEntry logEntry = new LogEntry(0, "hello rocksdb");
        System.out.println(logEntry);
        byte[] bytes = JSON.toJSONBytes(logEntry);
        LogEntry res = JSON.parseObject(bytes, LogEntry.class);
        System.out.println(res);
    }
}

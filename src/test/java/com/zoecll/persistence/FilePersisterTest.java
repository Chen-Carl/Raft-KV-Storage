package com.zoecll.persistence;

import org.junit.Test;

public class FilePersisterTest {
    private FilePersister filePersister;

    public FilePersisterTest() {
        filePersister = new FilePersister();
        filePersister.createSnapshot(1024 * 1024);
    }

    @Test
    public void testFilePersister() {
        String data1 = "test data1";
        String data2 = "test data2";
        filePersister.write(data1.getBytes());
        filePersister.write(data2.getBytes());
        byte[] res1 = filePersister.read(0);
        String resData1 = new String(res1);
        System.out.println(resData1);
        filePersister.saveSnapshot();
        filePersister.readSnapshot();
        byte[] res2 = filePersister.read(0);
        String resData2 = new String(res2);
        System.out.println(resData2);
    }
}

package com.zoecll.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePersister {

    private static final Logger logger = LoggerFactory.getLogger(FilePersister.class);

    private Snapshot snapshot;
    private String filename;
    private int id;
    private ReadWriteLock mutex = new ReentrantReadWriteLock();

    public FilePersister() {
        this.filename = "default";
        this.snapshot = new Snapshot(0);
        this.id = 0;
        logger.warn("No id specified for FilePersister");

    }

    public FilePersister(String filename) {
        this.filename = filename;
        this.snapshot = new Snapshot(0);
        this.id = 0;
        logger.warn("No id specified for FilePersister");
    }

    public FilePersister(int id) {
        this.filename = "raft-" + Integer.toString(id);
        this.snapshot = new Snapshot(0);
        this.id = id;
    }
    
    public void readSnapshot() {
        // read from file
        File folder = new File("snapshots");
        File[] files = folder.listFiles();
        File latestFile = null;
        for (File file : files) {
            if (file.isFile() 
                && (latestFile == null || file.getName().compareTo(latestFile.getName()) > 0)
                && file.getName().startsWith("raft-" + id)) {
                latestFile = file;
            }
        }
        if (latestFile != null) {
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(new FileInputStream(latestFile));
                synchronized (mutex.writeLock()) {
                    snapshot = (Snapshot) ois.readObject();
                }
                ois.close();
            } catch (FileNotFoundException e) {
                logger.error("Snapshot file not found");
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Internal error: IOException");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                logger.error("Snapshot file corrupted");
                e.printStackTrace();
            }
        }
    }
    
    public void saveSnapshot() {
        File file = new File("snapshots/" + filename + "." + Long.toString(System.currentTimeMillis()) + ".snapshot");
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            synchronized (mutex.readLock()) {
                oos.writeObject(snapshot);
            }
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void createSnapshot(int size) {
        snapshot = new Snapshot(size);
    }

    public void write(byte[] data, int offset) {
        synchronized (mutex.writeLock()) {
            snapshot.write(data, offset);
        }
    }

    public void write(byte[] data) {
        synchronized (mutex.writeLock()) {
            snapshot.write(data, snapshot.size());
        }
    }

    public byte[] read(int offset, int length) {
        synchronized (mutex.readLock()) {
            return snapshot.read(offset, length);
        }
    }

    public byte[] read(int offset) {
        return read(offset, snapshot.size() - offset);
    }

    public void setLastIncludedIndex(int index) {
        synchronized (mutex.writeLock()) {
            snapshot.setLastIncludedIndex(index);
        }
    }

    public void setLastIncludedTerm(int term) {
        synchronized (mutex.writeLock()) {
            snapshot.setLastIncludedTerm(term);
        }
    }

    public ArrayList<String> getCommands() {
        synchronized (mutex.readLock()) {
            return snapshot.getCommands();
        }
    }
}

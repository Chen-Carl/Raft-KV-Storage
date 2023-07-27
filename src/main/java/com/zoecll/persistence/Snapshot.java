package com.zoecll.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import protobuf.RaftRPCProto.LogEntry;

@Data
public class Snapshot implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Snapshot.class);
    
    private byte[] data;
    private int lastIncludedIndex;
    private int lastIncludedTerm;
    private int length;

    public Snapshot(int size) {
        this.lastIncludedIndex = -1;
        this.lastIncludedTerm = -1;
        this.data = new byte[size];
        this.length = 0;
    }

    public void write(byte[] data, int offset) {
        if (offset + data.length > this.data.length) {
            logger.error("Snapshot write failed: out of bound");
            return;
        }
        System.arraycopy(data, 0, this.data, offset, data.length);
        this.length += data.length;
    }

    public byte[] read(int offset, int length) {
        if (offset + length > this.length) {
            logger.warn("Snapshot read deficiency: no enough data");
            length = this.length - offset;
        }
        byte[] res = new byte[length];
        System.arraycopy(data, offset, res, 0, length);
        return res;
    }

    public int size() {
        return this.length;
    }

    public ArrayList<String> getCommands() {
        try {
            ObjectInputStream ois;
            ois = new ObjectInputStream(new ByteArrayInputStream(data));
            ArrayList<LogEntry> entries = (ArrayList<LogEntry>) ois.readObject();
            ois.close();
            ArrayList<String> commands = new ArrayList<>();
            for (LogEntry entry : entries) {
                commands.add(entry.getCommand());
            }
            return commands;
        } catch (IOException e) {
            logger.error("Deserialize failed: IOException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.error("Deserialize failed: ClassNotFoundException");
            e.printStackTrace();
        }
        return null;
    }
}

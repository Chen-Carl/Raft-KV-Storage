package com.zoecarl.common;

public class LogEntry implements Comparable<LogEntry> {
    private int index;
    private int term;
    private String key;
    private String value;

    public LogEntry(int term) {
        this.term = term;
    }

    public LogEntry(int term, String key, String value) {
        this.term = term;
        this.key = key;
        this.value = value;
    }

    public void setIndex(int index) {
        this.index = index;
    } 

    public int getTerm() {
        return term;
    }

    public int getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "index=" + index +
                ", term=" + term +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public int compareTo(LogEntry log) {
        if (log == null) {
            return -1;
        }
        if (this.getTerm() != log.getTerm()) {
            return this.getTerm() - log.getTerm();
        }
        return this.getIndex() - log.getIndex();
    }
}

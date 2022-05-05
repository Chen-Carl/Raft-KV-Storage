package com.zoecarl.common;

public class LogEntry implements Comparable<LogEntry> {
    private int index;
    private int term;
    private String content;

    public LogEntry(int term) {
        this.term = term;
    }

    public LogEntry(int term, String content) {
        this.term = term;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "index=" + index +
                ", term=" + term +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public int compareTo(LogEntry log) {
        if (log == null) {
            return -1;
        }
        if (this.getIndex() > ((LogEntry) log).getIndex()) {
            return 1;
        }
        return -1;
    }
}

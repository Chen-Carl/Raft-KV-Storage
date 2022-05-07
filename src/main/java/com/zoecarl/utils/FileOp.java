package com.zoecarl.utils;

public class FileOp {
    public static String readFile(String filename) {
        StringBuilder sb = new StringBuilder();
        try {
            java.io.FileReader fr = new java.io.FileReader(filename);
            int c;
            while ((c = fr.read()) != -1) {
                sb.append((char) c);
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}

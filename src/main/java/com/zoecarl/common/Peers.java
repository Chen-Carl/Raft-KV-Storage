package com.zoecarl.common;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;

public class Peers implements Serializable {
    public class Peer implements Serializable {
        private final String addr;

        public Peer(String addr) {
            this.addr = addr;
        }

        public Peer(String hostname, int port) {
            this.addr = hostname + ":" + port;
        }

        public String getAddr() {
            return addr;
        }

        @Override
        public String toString() {
            return "Peer{" + "addr=" + addr + '}';
        }

        @Override
        public boolean equals(Object p) {
            if (this == p) {
                return true;
            }
            if (p == null || getClass() != p.getClass()) {
                return false;
            }
            Peer peer = (Peer) p;
            return addr.equals(peer.getAddr());
        }
    }

    private List<Peer> list = new ArrayList<>();
    private volatile Peer leader;
    private volatile Peer self;

    public void addPeer(Peer peer) {
        list.add(peer);
    }

    public void removePeer(Peer peer) {
        list.remove(peer);
    }

    public void setSelf(String hostname, int port) {
        self = new Peer(hostname, port);
    }

    public void setSelf(Peer self) {
        this.self = self;
    }

    public Peer getSelf() {
        return self;
    }

    public List<Peer> getPeerList() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public boolean count(Peer peer) {
        return list.contains(peer);
    }
}

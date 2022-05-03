package com.zoecarl.common;

import java.util.List;
import java.util.ArrayList;

public class Peers {
    public class Peer {
        private final String addr;

        public Peer(String addr) {
            this.addr = addr;
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

    public Peer getSelf() {
        return self;
    }

    public List<Peer> getPeerList() {
        return list;
    }

    public int size() {
        return list.size();
    }
}

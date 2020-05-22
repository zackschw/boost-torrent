package dev.zackschw.boosttorrent;

import java.io.DataInputStream;

public class PeerConnectionIn {
    private final Peer peer;
    private final DataInputStream din;

    PeerConnectionIn (Peer peer, DataInputStream din) {
        this.peer = peer;
        this.din = din;
    }
}

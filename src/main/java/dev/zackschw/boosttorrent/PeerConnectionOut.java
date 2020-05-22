package dev.zackschw.boosttorrent;

import java.io.DataOutputStream;

public class PeerConnectionOut {
    private final Peer peer;
    private final DataOutputStream dout;

    PeerConnectionOut (Peer peer, DataOutputStream dout) {
        this.peer = peer;
        this.dout = dout;
    }
}

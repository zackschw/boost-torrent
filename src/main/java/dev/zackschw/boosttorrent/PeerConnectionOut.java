package dev.zackschw.boosttorrent;

import java.io.DataOutputStream;

public class PeerConnectionOut {
    private final Peer peer;
    private final DataOutputStream dout;

    PeerConnectionOut (Peer peer, DataOutputStream dout) {
        this.peer = peer;
        this.dout = dout;
    }

    void sendChoke() {

    }

    void sendUnchoke() {

    }

    void sendInterested() {

    }

    void sendUninterested() {

    }

    void sendHave(int piece) {

    }

    void sendBitfield(byte[] bitmap) {

    }
    
    void sendRequest(int piece, int begin, int length) {

    }

    void sendPiece(int piece, int begin, byte[] block) {

    }

    void sendCancel(int piece, int begin, int length) {

    }
}

package dev.zackschw.boosttorrent;

import java.io.DataOutputStream;
import java.io.IOException;

public class PeerConnectionOut {
    private final Peer peer;
    private final DataOutputStream dout;
    private final Object sendLock;

    PeerConnectionOut (Peer peer, DataOutputStream dout) {
        this.peer = peer;
        this.dout = dout;

        sendLock = new Object();
    }

    void sendChoke() {
        synchronized (sendLock) {
            try {
                Message.sendChoke(dout);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendUnchoke() {
        synchronized (sendLock) {
            try {
                Message.sendUnchoke(dout);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendInterested() {
        synchronized (sendLock) {
            try {
                Message.sendInterested(dout);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendNotInterested() {
        synchronized (sendLock) {
            try {
                Message.sendNotInterested(dout);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendHave(int piece) {
        synchronized (sendLock) {
            try {
                Message.sendHave(dout, piece);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendBitfield(byte[] bitmap) {
        synchronized (sendLock) {
            try {
                Message.sendBitfield(dout, bitmap);
            } catch (IOException e) {
                disconnect();
            }
        }
    }
    
    void sendRequest(int piece, int begin, int length) {
        synchronized (sendLock) {
            try {
                Message.sendRequest(dout, piece, begin, length);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendPiece(int piece, int begin, byte[] block) {
        synchronized (sendLock) {
            try {
                Message.sendPiece(dout, piece, begin, block);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    void sendCancel(int piece, int begin, int length) {
        synchronized (sendLock) {
            try {
                Message.sendCancel(dout, piece, begin, length);
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    /**
     * Called when we are disconnecting from this peer.
     */
    void disconnect() {
        peer.disconnect();
    }
}

package dev.zackschw.boosttorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Peer {
    private final PeerAddress peerAddress;
    private final MetadataInfo meta;
    private final byte[] myPeerID;

    private DataInputStream din;
    private DataOutputStream dout;

    private byte[] peerID;

    /**
     * Creates an unconnected peer.
     */
    public Peer(PeerAddress peerAddress, MetadataInfo meta, byte[] myPeerID) {
        this.peerAddress = peerAddress;
        this.meta = meta;
        this.myPeerID = myPeerID;
    }

    void runConnection() {
        try {
            Socket sock = new Socket(peerAddress.getAddress(), peerAddress.getPort());
            din = new DataInputStream(sock.getInputStream());
            dout = new DataOutputStream(sock.getOutputStream());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handshakes the peer and sets peerID on success.
     */
    private boolean handshake() {
        // TODO
        return false;
    }
}

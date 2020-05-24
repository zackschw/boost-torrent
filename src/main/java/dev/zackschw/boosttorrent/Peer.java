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
    private PeerState state;

    private byte[] peerID;

    /**
     * Creates an unconnected peer.
     */
    public Peer(PeerAddress peerAddress, MetadataInfo meta, byte[] myPeerID) {
        this.peerAddress = peerAddress;
        this.meta = meta;
        this.myPeerID = myPeerID;
    }

    /**
     * @return PeerState object of the peer
     */
    PeerState getState() {
        return state;
    }

    void runConnection(PeerCoordinator coordinator) {
        try {
            Socket sock = new Socket(peerAddress.getAddress(), peerAddress.getPort());
            din = new DataInputStream(sock.getInputStream());
            dout = new DataOutputStream(sock.getOutputStream());

            /* Handshake the peer */
            handshake();

            /* Set up reading thread */
            PeerConnectionIn cin = new PeerConnectionIn(this, din);
            PeerConnectionOut cout = new PeerConnectionOut(this, dout);
            state = new PeerState(this, cin, cout, meta, coordinator);

            /* Send first messages */

            /* Run! */

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handshakes the peer and sets peerID on success.
     */
    private void handshake() {
        // TODO
    }
}

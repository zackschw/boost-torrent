package dev.zackschw.boosttorrent;

import dev.zackschw.boosttorrent.bencode.BencodeException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Peer {
    private final PeerAddress peerAddress;
    private final MetadataInfo meta;
    private final byte[] myPeerID;

    private DataInputStream din;
    private DataOutputStream dout;

    private PeerState state;
    private PeerConnectionIn cin;
    private PeerConnectionOut cout;

    private byte[] peerID;

    /**
     * Creates an unconnected peer.
     */
    public Peer(PeerAddress peerAddress, MetadataInfo meta, byte[] myPeerID) {
        this.peerAddress = peerAddress;
        this.meta = meta;
        this.myPeerID = myPeerID;
    }


    PeerState getState() {
        return state;
    }

    PeerConnectionIn getPeerConnectionIn() {
        return cin;
    }

    PeerConnectionOut getPeerConnectionOut() {
        return cout;
    }

    public void disconnect() {
        if (cin != null)
            cin.disconnect();

        if (cout != null)
            cout.disconnect();
    }

    public void runConnection(PeerCoordinator coordinator, Bitvector myBitfield) {
        try {
            Socket sock = new Socket(peerAddress.getAddress(), peerAddress.getPort());
            din = new DataInputStream(sock.getInputStream());
            dout = new DataOutputStream(sock.getOutputStream());

            /* Handshake the peer (noting that receive handshake will set their peerID) */
            sendHandshake();
            recvHandshake();

            /* Set up reading */
            cin = new PeerConnectionIn(this, din);
            cout = new PeerConnectionOut(this, dout);
            state = new PeerState(this, cin, cout, meta, coordinator);

            coordinator.onConnected(this);

            /* Send first messages */
            if (myBitfield != null && !myBitfield.isEmpty()) {
                cout.sendBitfield(myBitfield.toByteArray());
            }

            /* Run! */
            cin.run();

        } catch (IOException ignore) {
        } finally {
            /* Disconnect */
            coordinator.onDisconnected(this);
        }
    }


    /**
     * Initiate handshake with the peer
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    private void sendHandshake() throws IOException {
        //pstrlen -> send byte 19
        dout.write(19);

        //pstr -> send string "BitTorrent protocol"
        dout.write("BitTorrent protocol".getBytes(StandardCharsets.UTF_8));

        //reserved -> send 8 empty bytes
        byte[] reserved = new byte[8];
        dout.write(reserved);

        //info_hash -> send hash from MetadataInfo
        dout.write(meta.getInfoHash());

        //peer_id -> send peer ID
        dout.write(myPeerID);

        dout.flush();
    }

    /**
     * Set peerID after verifying integrity of received handshake
     * @throws IOException if an I/O error occurs reading from the input stream
     */
    private void recvHandshake() throws IOException {
        /* Verify that pstrlen is 19 */
        int pstrlen = din.readInt();
        if (pstrlen != 19) {
            throw new IOException("Handshake pstrlen is invalid. Expected: 19. Received: " + pstrlen);
        };

        /* Verify that pstr is "BitTorrent protocol" */
        byte[] pstrBytes = new byte[19];
        din.readFully(pstrBytes);
        String pstr = new String(pstrBytes, StandardCharsets.UTF_8);
        if (!pstr.equals("BitTorrent protocol")) {
            throw new IOException("Handshake pstr is invalid. Expected: \"BitTorrent protocol\". Received: " + pstr);
        };

        /* Read reserved bytes */
        byte[] reserved = new byte[8];
        din.readFully(reserved);

        /* Verify that the info hash value matches what was expected */
        byte[] info_hash = new byte[20];
        din.readFully(info_hash);
        if (!Arrays.equals(meta.getInfoHash(), info_hash)) {
            throw new IOException("Handshake info hash is invalid. Expected: " + Arrays.toString(meta.getInfoHash()) + ". Received: " + Arrays.toString(info_hash));
        };

        /* Read peer id */
        peerID = new byte[20];
        din.readFully(peerID);
    }


    /**
     * Sends a have message to the peer
     * @param piece index of the completed piece
     */
    public void sendHave(int piece) {
        if (state != null)
            state.onHaveFinishedPiece(piece);
    }
}

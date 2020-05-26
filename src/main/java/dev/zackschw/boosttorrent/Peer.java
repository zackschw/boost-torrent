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



    void runConnection(PeerCoordinator coordinator, Bitvector myBitfield) {
        try {
            Socket sock = new Socket(peerAddress.getAddress(), peerAddress.getPort());
            din = new DataInputStream(sock.getInputStream());
            dout = new DataOutputStream(sock.getOutputStream());

            /* Handshake the peer (noting that receive handshake will set their peerID) */
            sendHandshake(dout);
            recvHandshake(din);

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

            /* Disconnect */
            coordinator.onDisconnected(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void disconnect() {
        cin.disconnect();
        cout.disconnect();
    }


    /**
     * Initiate handshake with the peer
     * @param dout data output stream for the handshake bytes
     * @throws IOException
     */
    private void sendHandshake(DataOutputStream dout) throws IOException {
        //pstrlen -> send byte 19
        dout.write(19);

        //pstr -> send string "BitTorrent protocol"
        dout.write("BitTorrent protocol".getBytes(StandardCharsets.UTF_8));

        //reserved -> send 8 empty bytes
        dout.writeLong(0);

        //info_hash -> send hash from MetadataInfo
        dout.write(meta.getInfoHash());

        //peer_id -> send peer ID
        dout.write(myPeerID);
    }

    /**
     * Set peerID after verifying integrity of received handshake
     * @param din data input stream for the handshake bytes
     * @throws IOException
     */
    private void recvHandshake(DataInputStream din) throws IOException {
        //verify that pstrlen is 19
        int pstrlen = din.readInt();
        if (pstrlen != 19) {
            throw new IOException("Handshake pstrlen is invalid. Expected: 19. Received: " + pstrlen);
        };

        //verify that pstr is "BitTorrent protocol"
        byte[] pstr = new byte[19];
        din.readFully(pstr);
        if (Arrays.toString(pstr) != "BitTorrent protocol") {
            throw new IOException("Handshake pstr is invalid. Expected: \"BitTorrent protocol\". Received: " + Arrays.toString(pstr));
        };

        //verify that reserved bytes are empty
        long reserved = din.readLong();
        if (reserved != 0) {
            throw new IOException("Handshake reserved bytes (displayed as long int) are invalid. Expected: 0. Received: " + reserved);
        }

        //verify that the info hash value matches what was expected
        byte[] info_hash = new byte[20];
        din.readFully(info_hash);
        if (!Arrays.equals(meta.getInfoHash(), info_hash)) {
            throw new IOException("Handshake info hash is invalid. Expected: " + Arrays.toString(meta.getInfoHash()) + ". Received: " + Arrays.toString(info_hash));
        };

        //naively take 20 bytes as their peerID, since the rest of the handshake was correct
        peerID = new byte[20];
        din.readFully(peerID);
    }
}

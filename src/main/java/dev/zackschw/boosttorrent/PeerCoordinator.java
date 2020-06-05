package dev.zackschw.boosttorrent;

import dev.zackschw.boosttorrent.tracker.TrackerCoordinator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeerCoordinator {
    private final byte[] myPeerID;
    private final MetadataInfo meta;
    private final Storage storage;
    private final PeerAcceptor peerAcceptor;
    private final TrackerCoordinator tracker;
    private int listenerPort;
    private long uploaded;
    private long downloaded;
    private long left;

    private final List<PeerAddress> potentialPeers;
    private final List<Peer> peers;
    private final List<Integer> outstandingPieces;

    private static int MAX_WANTED_PEERS = 30;
    private static int MAX_PEERS = 50;

    public PeerCoordinator(byte[] myPeerID, MetadataInfo meta, Storage storage) {
        this.myPeerID = myPeerID;
        this.meta = meta;
        this.storage = storage;

        this.peerAcceptor = new PeerAcceptor(this, meta, myPeerID);
        this.tracker = new TrackerCoordinator(meta, this);
        this.peers = new ArrayList<>(MAX_PEERS);
        this.potentialPeers = new ArrayList<>();
        this.outstandingPieces = new ArrayList<>();
    }

    /**
     * Binds to a port to listen for connections, Searches for peers via tracker,
     * and initiate connections with up to MAX_WANTED_PEERS peers.
     * @throws IOException if I/O error occurs binding to a port to listen for connections
     */
    public void initiateConnections() throws IOException {
        /* Start listener */
        listenerPort = peerAcceptor.runListener();

        /* Query tracker */
        List<PeerAddress> received = tracker.sendStarted();
        potentialPeers.addAll(received);
        // TODO assert size > 0

        /* Add peer */
        synchronized (potentialPeers) {
            for (int i = 0; i < MAX_WANTED_PEERS && potentialPeers.size() > 0; i++) {
                PeerAddress address = potentialPeers.remove(0);
                Peer peer = new Peer(address, meta, myPeerID);
                addPeer(peer);
            }
        }
    }

    /**
     * Runs peer connection in a new thread. Peer will be added to list of active peers upon successful handshake,
     * via onConnected().
     */
    public void addPeer(Peer peer) {
        /* Check that we want connections */
        if (peers.size() >= MAX_PEERS) {
            return;
        }

        /* Run connection in a new thread */
        Thread t = new Thread(() -> peer.runConnection(this, storage.getMyBitfield()));
        t.start();
    }

    /**
     * Returns true if the client has the given piece, otherwise false.
     * @param index piece to check for
     */
    public boolean havePiece(int index) {
        Bitvector myBitfield = storage.getMyBitfield();
        return myBitfield.isSet(index);
    }

    /**
     * Returns true if the client is interested in any piece of the bitvector, otherwise false
     * @param bitvector peer's bitvector to check for interest for
     */
    public boolean wantAnyPiece(Bitvector bitvector) {
        Bitvector myBitfield = storage.getMyBitfield();
        if (myBitfield == null)
            return true;

        int size = bitvector.getSize();

        for (int i=0; i < size; i++) {
            if (!myBitfield.isSet(i) && bitvector.isSet(i)) {
                return true;
            }
        }

        return false;
    }


    public void gotRequest() {
        // TODO
    }

    /**
     * Writes finished piece to storage and sends Have message to all peers
     */
    public void onFinishedPiece(Piece piece) {
        // TODO write to storage

        /* Remove from outstanding pieces */
        synchronized (outstandingPieces) {
            outstandingPieces.remove(Integer.valueOf(piece.index));
        }

        /* Send have to all peers */
        synchronized (peers) {
            for (Peer p : peers) {
                p.sendHave(piece.index);
            }
        }
    }

    /**
     * On successful connection, add peer to active peers
     * @param peer peer that was connected to
     */
    public void onConnected(Peer peer) {
        synchronized (peers) {
            /* Check that this peer was not already added */
            boolean present = false;
            for (Peer p : peers) {
                if (Arrays.equals(peer.getPeerID(), p.getPeerID())) {
                    present = true;
                    break;
                }
            }

            if (!present) {
                peers.add(peer);
            } else {
                peer.disconnect();
            }
        }
    }

    /**
     * On disconnect, remove from active peers add a new peer if needed
     */
    public void onDisconnected(Peer peer) {
        synchronized (peers) {
            peers.remove(peer);
        }

        if (peers.size() < MAX_WANTED_PEERS) {
            synchronized (potentialPeers) {
                if (potentialPeers.size() > 0) {
                    PeerAddress address = potentialPeers.remove(0);
                    Peer newPeer = new Peer(address, meta, myPeerID);
                    addPeer(newPeer);
                }
            }
        }
    }

    /**
     * Returns a new piece index to request that the client does not have, and that the peer does have.
     * @param peerBitfield the bitfield of the peer to send the requests to.
     * @return the piece index, or -1 if no such piece exists
     */
    public int getNextPieceToRequest(Bitvector peerBitfield) {
        Bitvector myBitfield = storage.getMyBitfield();

        /* Find a piece that has not been requested yet and that the peer has */
        synchronized (outstandingPieces) {
            for (int i = 0; i < myBitfield.getSize(); i++) {
                if (!myBitfield.isSet(i) && peerBitfield.isSet(i) && !outstandingPieces.contains(i)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public int getLocalPort() {
        return listenerPort;
    }

    public byte[] getMyPeerID() {
        return myPeerID;
    }

    public long getUploaded() {
        return uploaded;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public long getLeft() {
        return left;
    }
}

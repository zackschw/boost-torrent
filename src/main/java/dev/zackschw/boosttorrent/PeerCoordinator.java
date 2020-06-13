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
    private final Fulfiller fulfiller;
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

        peerAcceptor = new PeerAcceptor(this, meta, myPeerID);
        tracker = new TrackerCoordinator(meta, this);
        fulfiller = new Fulfiller(this);
        peers = new ArrayList<>(MAX_PEERS);
        potentialPeers = new ArrayList<>();
        outstandingPieces = new ArrayList<>();
        left = meta.getTotalFileBytes();
    }

    /**
     * Binds to a port to listen for connections, Searches for peers via tracker,
     * and initiate connections with up to MAX_WANTED_PEERS peers.
     * @throws IOException if I/O error occurs binding to a port to listen for connections
     */
    public void initiateConnections() throws IOException {
        /* Start listener */
        listenerPort = peerAcceptor.runListener();

        /* Start fulfiller */
        fulfiller.runFulfiller();

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
     * @return true if the client attempted to add the peer, false if the client is already connected to MAX_PEERS peers
     */
    public boolean addPeer(Peer peer) {
        /* Check that we want connections */
        if (peers.size() >= MAX_PEERS) {
            return false;
        }

        /* Run connection in a new thread */
        Thread t = new Thread(() -> peer.runConnection(this, storage.getMyBitfield()));
        t.start();
        return true;
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


    /**
     * Adds request to queue to fulfill.
     */
    public void gotRequest(int index, int begin, int len, Peer peer) {
        fulfiller.onReceivedRequest(index, begin, len, peer);
    }

    /**
     * Writes finished piece to storage and sends Have message to all peers
     */
    public void onFinishedPiece(Piece piece) {
        /* Write to storage */
        storage.writePiece(piece);

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
                    outstandingPieces.add(i);
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Returns a list of peers containing all peers that are currently unchoked by the client.
     * If no peers are unchoked, the returned list will be of size 0.
     */
    public List<Peer> getPeersAmUnchoking() {
        List<Peer> unchokingList = new ArrayList<>(4);

        synchronized (peers) {
            for (Peer p: peers) {
                if (!p.getAmChoking()) {
                    unchokingList.add(p);
                }
            }
        }

        return unchokingList;
    }

    /**
     * @return the port the client is listening for connections on
     */
    public int getLocalPort() {
        return listenerPort;
    }

    /**
     * @return the client's 20-byte peer id
     */
    public byte[] getMyPeerID() {
        return myPeerID;
    }

    /**
     * @return the total number of bytes uploaded to peers since starting this download
     */
    public long getUploaded() {
        return uploaded;
    }

    /**
     * @return the total number of bytes downloaded from peers since starting this download
     */
    public long getDownloaded() {
        return downloaded;
    }

    /**
     * @return the total number of bytes left to download until all pieces are complete
     */
    public long getLeft() {
        return left;
    }

    /**
     * Adds to the statistic of the number of bytes uploaded to peers.
     * @param uploaded number of bytes uploaded
     */
    public void incrementUploaded(int uploaded) {
        // TODO
    }

    /**
     * Adds to the statistic of the number of bytes downloaded from peers.
     * @param downloaded number of bytes downloaded
     */
    public void incrementDownloaded(int downloaded) {
        // TODO
    }

    /**
     * Resets the statistics of all peer's uploaded to and downloaded from bytes. Used for unchoking algorithm.
     */
    public void resetPeersUploadedDownloaded() {
        // TODO
    }
}

package dev.zackschw.boosttorrent;

import java.util.ArrayList;
import java.util.List;

public class PeerState {
    private final Peer peer;
    private final PeerConnectionIn cin;
    private final PeerConnectionOut cout;
    private final MetadataInfo meta;
    private final PeerCoordinator coordinator;

    private boolean amChoking; // the client is choking the peer
    private boolean peerChoking; // the peer is choking the client
    private boolean amInterested; // the client is interested in the peer
    private boolean peerInterested; // the peer is interested in the client

    private long downloaded;
    private long uploaded;

    private Bitvector bitfield; // the peer's bitfield

    private final List<Piece> workingPieces; // the pieces the client is requesting from this peer
    private boolean resendRequests; // resend requests after being choked and then unchoked

    PeerState(Peer peer, PeerConnectionIn cin, PeerConnectionOut cout, MetadataInfo meta, PeerCoordinator coordinator) {
        this.peer = peer;
        this.cin = cin;
        this.cout = cout;
        this.meta = meta;
        this.coordinator = coordinator;

        amChoking = true;
        peerChoking = true;
        amInterested = false;
        peerInterested = false;

        workingPieces = new ArrayList<>(2);
        resendRequests = false;
    }

    /**
     * On receive CHOKE or UNCHOKE message
     */
    void onChokeMessage(boolean choking) {
        peerChoking = choking;

        if (peerChoking) {
            resendRequests = true;
        }

        if (!peerChoking && amInterested) {
            startRequesting();
        }
    }

    /**
     * On receive INTERESTED or UNINTERESTED message
     */
    void onInterestMessage(boolean interested) {
        peerInterested = interested;
    }

    /**
     * On receive HAVE message
     */
    void onHaveMessage(int piece) {
        /* Check if the other side sent a bitfield */
        if (bitfield == null) {
            if (meta.getNumPieces() != 0) {
                bitfield = new Bitvector(meta.getNumPieces());
            } else {
                /* TODO wait for bitvector */
                return;
            }
        }

        /* Sanity check */
        if (piece <= meta.getNumPieces() && piece >= 0) {
            bitfield.setBit(piece);
            /* If we do not have the piece yet then we are now interested in the peer */
            if (!amInterested && !coordinator.havePiece(piece))
                setAmInterested(true);
        } else {
            peer.disconnect();
        }
    }

    /**
     * On receive BITFIELD message
     */
    void onBitfieldMessage(byte[] bitmap) {
        if (bitfield == null) {
            if (meta.getNumPieces() == 0) {
                /* If we don't know the number of pieces, liberally accept the bitfield */
                bitfield = new Bitvector(bitmap.length * 8, bitmap);
                /* Set initial interested status */
                setAmInterested(coordinator.wantAnyPiece(bitfield));
            } else {
                /* If we do know the number of pieces, ensure it is correct */
                if (meta.getNumPieces() / 8 + (meta.getNumPieces() % 8 == 0 ? 0 : 1) == bitmap.length) {
                    bitfield = new Bitvector(meta.getNumPieces(), bitmap);
                    /* Set initial interested status */
                    setAmInterested(coordinator.wantAnyPiece(bitfield));
                } else {
                    peer.disconnect();
                }
            }
        } else {
            /* Already received a bitfield message */
            peer.disconnect();
        }
    }


    /**
     * On receive REQUEST message
     */
    void onRequestMessage(int piece, int begin, int length) {
        coordinator.gotRequest(piece, begin, length, peer);
    }

    /**
     * On receive CANCEL message
     */
    void onCancelMessage(int piece, int begin, int length) {
        coordinator.gotCancel(piece, begin, length, peer);
    }


    /**
     * On receive PIECE message
     */
    void onPieceMessage(Piece piece, int begin) {
        if (piece == null) {
            System.out.println("Received unexpected piece.");
            peer.disconnect();
            return;
        }

        piece.onReceivedBlock(begin);

        // TODO when to start requesting next piece?
        if (!peerChoking && piece.getNumReceivedBlocks() == piece.length*3/4)
            requestNextPiece();

        if (piece.receivedAllBlocks()) {
            /* Remove from working pieces */
            synchronized (workingPieces) {
                workingPieces.remove(piece);
            }

            /* Check hash */
            if (piece.checkHash()) {
                coordinator.onFinishedPiece(piece);
            } else {
                /* Disconnect on bad hash */
                peer.disconnect();
            }
        }
    }

    /**
     * Returns the Piece object that the client is requesting from this peer, specified by the piece index.
     * @param index zero-based index of the piece
     * @return the piece object, or null if it does not exist
     */
    Piece getWorkingPiece(int index) {
        synchronized (workingPieces) {
            for (Piece p: workingPieces) {
                if (p.index == index) {
                    return p;
                }
            }
        }

        return null;
    }


    /**
     * On completed a piece, send HAVE to this peer
     */
    void onHaveFinishedPiece(int piece) {
        /* TODO send cancels for any outstanding requests */

        /* Send Have */
        cout.sendHave(piece);

        /* Recalculate interest */
        setAmInterested(coordinator.wantAnyPiece(bitfield));
    }


    /**
     * Starts requesting from a peer. Client must be unchoked before calling.
     */
    private void startRequesting() {
        /* Resend requests if unchoked after being choked */
        if (resendRequests) {
            synchronized (workingPieces) {
                for (Piece piece: workingPieces) {
                    /* Send all outstanding requests except last request */
                    int lastOff = piece.length - Piece.BLOCK_LENGTH;
                    for (int begin=0; begin < lastOff; begin += Piece.BLOCK_LENGTH) {
                        if (!piece.isBlockReceived(begin))
                            cout.sendRequest(piece.index, begin, Piece.BLOCK_LENGTH);
                    }

                    /* Send outstanding last request */
                    int lastLen = piece.index == meta.getNumPieces() - 1 ?
                            meta.getLastPieceLength() - Piece.BLOCK_LENGTH*(meta.getNumPieces() - 1) : Piece.BLOCK_LENGTH;
                    if (!piece.isBlockReceived(lastOff))
                        cout.sendRequest(piece.index, lastOff, lastLen);
                }
            }

            resendRequests = false;
        } else {
            requestNextPiece();
        }
    }

    /**
     * Requests all blocks in the next piece given by the coordinator.
     * Should only be called when the client is unchoked.
     */
    private void requestNextPiece() {
        /* Get next piece from coordinator */
        int index = coordinator.getNextPieceToRequest(bitfield);
        if (index == -1) {
            // TODO uninterested
            return;
        }
        int length = index == meta.getNumPieces() - 1 ? meta.getLastPieceLength() : meta.getPieceLength();
        Piece piece = new Piece(index, length, meta.getPieceHash(index));

        /* Add to working pieces */
        synchronized (workingPieces) {
            workingPieces.add(piece);
        }

        /* Send all requests except last request */
        int lastOff = piece.length - Piece.BLOCK_LENGTH;
        for (int begin=0; begin < lastOff; begin += Piece.BLOCK_LENGTH) {
            cout.sendRequest(piece.index, begin, Piece.BLOCK_LENGTH);
        }

        /* Send last request */
        int lastLen = piece.index == meta.getNumPieces() - 1 ?
                meta.getLastPieceLength() - Piece.BLOCK_LENGTH*(meta.getNumPieces() - 1) : Piece.BLOCK_LENGTH;
        cout.sendRequest(piece.index, lastOff, lastLen);
    }


    /**
     * @return boolean representing if the client is choking the peer
     */
    boolean getAmChoking() {
        return amChoking;
    }

    /**
     * @return boolean representing if the peer is choking the client
     */
    boolean getPeerChoking() {
        return peerChoking;
    }

    /**
     * @return boolean representing if the client is interested in the peer
     */
    boolean getAmInterested() {
        return amInterested;
    }

    /**
     * @return boolean representing if the peer is interested in the client
     */
    boolean getPeerInterested() {
        return peerInterested;
    }

    /**
     * Sets amInterested, ie if the client is interested in the peer.
     * If the interest status changes, a respective INTERESTED or NOTINTERESTED message is sent.
     * @param interested new interested status
     */
    private synchronized void setAmInterested(boolean interested) {
        if (!amInterested && interested) {
            amInterested = true;
            cout.sendInterested();

            if (amInterested && !peerChoking)
                startRequesting();
        } else if (amInterested && !interested) {
            amInterested = false;
            cout.sendNotInterested();
        }
    }

    /**
     * Gets the number of bytes downloaded from this peer since last call to resetUploadedDownloaded()
     */
    long getDownloaded() {
        return downloaded;
    }

    /**
     * Gets the number of bytes uploaded to this peer since last call to resetUploadedDownloaded()
     */
    long getUploaded() {
        return uploaded;
    }

    /**
     * Adds to the statistic of the number of bytes uploaded to this peer.
     * @param downloaded number of bytes downloaded
     */
    void incrementDownloaded(int downloaded) {
        this.downloaded += downloaded;
        coordinator.incrementDownloaded(downloaded);
    }

    /**
     * Adds to the statistic of the number of bytes uploaded to this peer.
     * @param uploaded number of bytes uploaded
     */
    void incrementUploaded(int uploaded) {
        this.uploaded += uploaded;
        coordinator.incrementUploaded(uploaded);
    }

    /**
     * Resets the statistics of this peer's uploaded to and downloaded from bytes. Used for unchoking algorithm.
     */
    void resetUploadedDownloaded() {
        downloaded = 0;
        uploaded = 0;
    }
}

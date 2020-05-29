package dev.zackschw.boosttorrent;

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

    private Bitvector bitfield; // the peer's bitfield

    private Piece workingPiece;

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
    }

    /**
     * On receive CHOKE or UNCHOKE message
     */
    void onChokeMessage(boolean choking) {
        peerChoking = choking;
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
        if (bitfield == null) {
            /* TODO Wait for bitfield */
            return;
        }

        /* Sanity check */
        if (piece <= meta.getNumPieces() && piece >= 0) {
            bitfield.setBit(piece);
            /* If we do not have the piece yet then we are now interested in the peer */
            if (!coordinator.havePiece(piece))
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

    }

    /**
     * On receive PIECE message
     */
    void onPieceMessage(int piece, int begin) {

    }

    /**
     * Returns the Piece object that the client is requesting from this peer, specified by the piece index.
     * @param index zero-based index of the piece
     * @return the piece object
     */
    Piece getWorkingPiece(int index) {
        // TODO
        return workingPiece;
    }

    /**
     * On receive CANCEL message
     */
    void onCancelMessage(int piece, int begin, int length) {

    }


    /**
     * On completed a piece, send HAVE to this peer
     */
    void onHaveFinishedPiece(int piece) {
        /* TODO send cancels for any outstanding requests */

        /* Send Have */
        cout.sendHave(piece);

        /* TODO add more requests and recalculate interest if no more requests are sent */
    }


    /**
     * Starts requesting from a peer. Client must be unchoked before calling.
     */
    void startRequesting() {
        // TODO
    }

    /**
     * Requests up to PIPELINE_REQUESTS outstanding requests
     */
    void addRequests() {

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
    void setAmInterested(boolean interested) {
        if (!amInterested && interested) {
            amInterested = true;
            cout.sendInterested();
        } else if (amInterested && !interested) {
            amInterested = false;
            cout.sendNotInterested();
        }
    }
}

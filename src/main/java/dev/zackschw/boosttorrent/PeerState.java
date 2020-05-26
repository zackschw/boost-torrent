package dev.zackschw.boosttorrent;

public class PeerState {
    private final Peer peer;
    private final PeerConnectionIn in;
    private final PeerConnectionOut out;
    private final MetadataInfo meta;
    private final PeerCoordinator coordinator;

    private boolean amChoking; // the client is choking the peer
    private boolean peerChoking; // the peer is choking the client
    private boolean amInterested; // the client is interested in the peer
    private boolean peerInterested; // the peer is interested in the client

    private Bitvector bitfield;

    PeerState(Peer peer, PeerConnectionIn in, PeerConnectionOut out, MetadataInfo meta, PeerCoordinator coordinator) {
        this.peer = peer;
        this.in = in;
        this.out = out;
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
            recalculateInterest();
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
                recalculateInterest();
            } else {
                /* If we do know the number of pieces, ensure it is correct */
                if (meta.getNumPieces() / 8 + (meta.getNumPieces() % 8 == 0 ? 0 : 1) == bitmap.length) {
                    bitfield = new Bitvector(meta.getNumPieces(), bitmap);
                    recalculateInterest();
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
    void onPieceMessage(int piece, int begin, byte[] block) {

    }

    /**
     * On receive CANCEL message
     */
    void onCancelMessage(int piece, int begin, int length) {

    }


    /**
     * Recalculates if the client is interested in the peer
     */
    void recalculateInterest() {
        // TODO
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
}

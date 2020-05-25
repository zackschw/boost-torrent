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
    void OnInterestMessage(boolean interested) {
        peerInterested = interested;
    }

    /**
     * On receive HAVE message
     */
    void onHaveMessage(int piece) {

    }

    /**
     * On receive BITFIELD message
     */
    void onBitfieldMessage(byte[] bitmap) {

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

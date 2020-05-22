package dev.zackschw.boosttorrent;

public class PeerState {
    private final Peer peer;
    private final PeerConnectionIn in;
    private final PeerConnectionOut out;
    private final MetadataInfo meta;
    private final PeerCoordinator coordinator;

    boolean amChoking; // the client is choking the peer
    boolean peerChoking; // the peer is choking the client
    boolean amInterested; // the client is interested in the peer
    boolean peerInterested; // the peer is interested in the client

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

    
}

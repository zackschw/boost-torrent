package dev.zackschw.boosttorrent;

import java.util.List;

public class PeerCoordinator {
    private byte[] myPeerID;
    private long uploaded;
    private long downloaded;
    private long left;

    private List<Peer> peers;

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

package dev.zackschw.boosttorrent;

import java.util.ArrayList;
import java.util.List;

public class Fulfiller {
    private final PeerCoordinator coordinator;
    private final List<Request> requests;
    private boolean stop;

    public Fulfiller(PeerCoordinator coordinator) {
        this.coordinator = coordinator;
        this.requests = new ArrayList<>();
        stop = false;
    }

    private static class Request {
        int index;
        int begin;
        int len;
        Peer peer;

        Request(int index, int begin, int len, Peer peer) {
            this.index = index;
            this.begin = begin;
            this.len = len;
            this.peer = peer;
        }
    }


    /**
     * Starts a new thread to send pieces to peers.
     */
    public void runFulfiller() {
        Thread t = new Thread(this::sendPiecesToPeers);
        t.start();
    }

    /**
     * Stops fulfiller thread.
     */
    public void stop() {
        stop = true;
    }

    /**
     * Adds request received from peer to fulfiller.
     */
    public void onReceivedRequest(int index, int begin, int len, Peer peer) {
        Request req = new Request(index, begin, len, peer);

        synchronized (requests) {
            requests.add(req);
        }
    }

    /**
     * Clears all outstanding requests to this peer.
     * Used when the client chokes a peer and will no longer fulfill requests.
     */
    public void clearRequestsFromPeer() {
        // TODO
    }

    private void sendPiecesToPeers() {
        while (!stop) {
            List<Peer> peersAmUnchoking = coordinator.getPeersAmUnchoking();
            // TODO maintain this with a set
            for (Peer p : peersAmUnchoking) {
                // TODO get first request from this peer and send the block
                Request r = requests.get(0);
                p.getState().incrementUploaded(r.len);
            }
        }
    }
}

package dev.zackschw.boosttorrent;

import java.util.ArrayList;
import java.util.List;

public class Fulfiller {
    private final PeerCoordinator coordinator;
    private final Storage storage;

    private Thread thread;
    private final List<Request> requests;
    private final List<Peer> unchokedPeers;
    private boolean stop;

    public Fulfiller(PeerCoordinator coordinator, Storage storage) {
        this.coordinator = coordinator;
        this.storage = storage;

        requests = new ArrayList<>();
        unchokedPeers = new ArrayList<>(4);
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

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Request)) {
                return false;
            }

            Request r = (Request) o;
            return r.index == index && r.begin == begin && r.len == len && r.peer == peer;
        }
    }


    /**
     * Starts a new thread to send pieces to peers.
     */
    public void runFulfiller() {
        thread = new Thread(this::sendPiecesToPeers);
        thread.start();
    }

    /**
     * Stops fulfiller thread.
     */
    public void stop() {
        stop = true;
        thread.interrupt();
    }

    /**
     * Adds request received from peer to fulfiller.
     */
    public void onReceivedRequest(int index, int begin, int len, Peer peer) {
        Request req = new Request(index, begin, len, peer);

        synchronized (requests) {
            requests.add(req);
        }

        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Fulfill requests to this peer.
     */
    public void onUnchokePeer(Peer peer) {
        synchronized (unchokedPeers) {
            unchokedPeers.add(peer);
        }
    }

    public void onReceivedCancel(int index, int begin, int len, Peer peer) {
        Request req = new Request(index, begin, len, peer);

        synchronized (requests) {
            requests.remove(req);
        }
    }

    /**
     * Clears all outstanding requests to this peer.
     * Used when the client chokes a peer and will no longer fulfill requests.
     */
    public void clearRequestsFromPeer(Peer peer) {
        synchronized (unchokedPeers) {
            unchokedPeers.remove(peer);
        }

        synchronized (requests) {
            requests.removeIf(request -> request.peer == peer);
        }
    }

    private void sendPiecesToPeers() {
        while (!stop) {
            /* Wait for requests to come in */
            while (requests.size() == 0) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            if (stop)
                break;

            synchronized (unchokedPeers) {
                for (Peer p : unchokedPeers) {
                    /* Get first request from this peer and send the block */
                    synchronized (requests) {
                        for (Request r : requests) {
                            if (r.peer == p) {
                                byte[] block = storage.readBlock(r.index, r.begin, r.len);
                                p.getPeerConnectionOut().sendPiece(r.index, r.begin, block);
                                p.getState().incrementUploaded(r.len);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}

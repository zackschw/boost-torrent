package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.MetadataInfo;
import dev.zackschw.boosttorrent.PeerAddress;
import dev.zackschw.boosttorrent.PeerCoordinator;

import java.util.ArrayList;
import java.util.List;

public class TrackerCoordinator {
    private final MetadataInfo meta;
    private final int port;
    private final PeerCoordinator coordinator;

    private final List<TrackerInfo> trackers = new ArrayList<>();


    public TrackerCoordinator(MetadataInfo meta, int port, PeerCoordinator coordinator) {
        this.meta = meta;
        this.port = port;
        this.coordinator = coordinator;

        if (meta.getAnnounceList() == null) {
            addTracker(meta.getAnnounce());
        } else {
            for (List<String> tier : meta.getAnnounceList()) {
                for (String url : tier) {
                    addTracker(url);
                }
            }
        }
    }

    private void addTracker(String url) {
        if (url.startsWith("udp://"))
            trackers.add(new UDPTrackerInfo(url, meta.getInfoHash(), port, coordinator));
        else
            trackers.add(new HTTPTrackerInfo(url, meta.getInfoHash(), port, coordinator));
    }



    /**
     * Send started event.
     * @return list of peers retrieved from tracker, or null if no successful connection was found.
     */
    public List<PeerAddress> sendStarted() {
        // TODO query asynchronously
        for (TrackerInfo tracker : trackers) {
            if (tracker.sendStarted())
                return tracker.getPeers();
        }

        return null;
    }


    public void sendCompleted() {
        for (TrackerInfo tracker : trackers) {
            tracker.sendCompleted();
        }
    }


    public void sendStopped() {
        for (TrackerInfo tracker : trackers) {
            tracker.sendStopped();
        }
    }


    /**
     * Send empty event.
     * @return list of peers retrieved from tracker, or null if no successful connection was found.
     */
    public List<PeerAddress> sendEmpty() {
        for (TrackerInfo tracker : trackers) {
            if (tracker.sendEmpty())
                return tracker.getPeers();
        }

        return null;
    }
}

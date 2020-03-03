package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.MetadataInfo;
import dev.zackschw.boosttorrent.PeerCoordinator;

import java.util.ArrayList;
import java.util.List;

public class TrackerClient {
    private final MetadataInfo meta;
    private final int port;
    private final PeerCoordinator coordinator;

    private final List<TrackerInfo> trackers = new ArrayList<>();


    public TrackerClient(MetadataInfo meta, int port, PeerCoordinator coordinator) {
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

    public boolean sendStarted() {
        for (TrackerInfo tracker : trackers) {
            if (tracker.sendStarted())
                return true;
        }

        return false;
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


    public boolean sendEmpty() {
        for (TrackerInfo tracker : trackers) {
            if (tracker.sendEmpty())
                return true;
        }

        return false;
    }
}

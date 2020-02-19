package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.Peer;
import dev.zackschw.boosttorrent.PeerAddress;

import java.util.List;

public interface TrackerInfo {

    /**
     * Send a started event to the tracker
     * @return true on successful connection, otherwise false
     */
    boolean sendStarted();

    /**
     * Send a completed event to the tracker
     * @return true on successful connection, otherwise false
     */
    boolean sendCompleted();

    /**
     * Send a stopped event to the tracker
     * @return true on successful connection, otherwise false
     */
    boolean sendStopped();

    /**
     * Send an empty event to the tracker
     * @return true on successful connection, otherwise false
     */
    boolean sendEmpty();

    /**
     * Gets the list of peers that were retrieved from this tracker.
     * Connection must be established first via sendStarted() and then any number of sendEmpty()
     * @return List of peers that were retrieved from this tracker.
     */
    List<PeerAddress> getPeers();
}

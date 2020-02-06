package dev.zackschw.boosttorrent.tracker;

public interface TrackerClient {
    boolean sendStarted();
    boolean sendCompleted();
    boolean sendStopped();
    boolean sendEmpty();
    boolean disconnect();

}

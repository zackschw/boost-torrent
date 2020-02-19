package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.MetadataInfo;
import dev.zackschw.boosttorrent.Peer;
import dev.zackschw.boosttorrent.PeerAddress;
import dev.zackschw.boosttorrent.PeerCoordinator;
import dev.zackschw.boosttorrent.bencode.BencodeException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UDPTrackerInfo implements TrackerInfo {
    private final String url; // eg "udp://tracker.leechers-paradise.org:6969"
    private final byte[] infoHash;
    private final int port;

    private final PeerCoordinator coordinator;

    private final List<PeerAddress> peers;
    private static Random random = new Random();


    public UDPTrackerInfo(String url, byte[] infoHash, int port, PeerCoordinator coordinator) {
        if (!url.startsWith("udp://"))
            throw new IllegalArgumentException("Invalid UDPTracker URL: " + url);

        this.url = url.substring(6); // string after "udp://"
        this.infoHash = infoHash;
        this.port = port;

        this.coordinator = coordinator;

        peers = new ArrayList<>();
    }


    @Override
    public boolean sendStarted() {
        try {
            return doRequest(EVENT_STARTED);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public boolean sendCompleted() {
        try {
            return doRequest(EVENT_COMPLETED);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public boolean sendStopped() {
        try {
            return doRequest(EVENT_STOPPED);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public boolean sendEmpty() {
        try {
            return doRequest(EVENT_NONE);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public List<PeerAddress> getPeers() {
        return peers;
    }


    /**
     * Sends request to the udp url
     * @param event event to send, which must be one of "STARTED", "STOPPED", "COMPLETED", or "NONE"
     * @return true on successful connection and response, otherwise false
     * @throws IOException on connection I/O error
     */
    private boolean doRequest(int event) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = null; // TODO parse url
        int port = 0; // TODO parse url

        /* Send and receive action: connect */
        long connectionID = sendConnectRequest(socket, address, port);

        /* Send and receive action: announce */
        try {
            sendAnnounceRequest(socket, address, port, connectionID, event);
        } catch (BencodeException e) {
            return false;
        }

        return true;
    }

    /**
     * Sends and receives action: connect
     * @return connectionID from connect response
     */
    private long sendConnectRequest(DatagramSocket socket, InetAddress address, int port) throws IOException {
        /* Send connect */
        int transactionID = getRandomTransactionID();
        byte[] connectBuffer = ByteBuffer.allocate(16).array(); // TODO construct the ByteBuffer
        DatagramPacket connectPacket = new DatagramPacket(connectBuffer, connectBuffer.length, address, port);
        socket.send(connectPacket);

        /* Receive response */
        byte[] receiveBuf = new byte[16];
        DatagramPacket responsePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        socket.receive(responsePacket);
        // TODO validate we received ACTION=CONNECT and our transactionID.
        return 0; // TODO return connection ID
    }

    private void sendAnnounceRequest(DatagramSocket socket, InetAddress address, int port, long connectionID, int event)
            throws IOException, BencodeException {
        /* Send announce */
        int transactionID = getRandomTransactionID();
        byte[] announceBuffer = ByteBuffer.allocate(98).array(); // TODO construct the ByteBuffer
        DatagramPacket announcePacket = new DatagramPacket(announceBuffer, announceBuffer.length, address, port);
        socket.send(announcePacket);

        if (event == EVENT_STARTED || event == EVENT_NONE) {
            /* Receive response */
            byte[] receiveBuf = new byte[1024]; // large enough to hold peers
            DatagramPacket responsePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            socket.receive(responsePacket);
            // TODO call decodePeers() here
        }
    }

    void decodePeers(byte[] receiveBuf, int receiveLength, int transactionID) {
        // TODO validate we received ACTION=ANNOUNCE and our transactionID

        // TODO decode peers and add each peer to List<PeerAddress> peers
    }


    private int getRandomTransactionID() {
        return random.nextInt();
    }

    private static long MAGIC_CONSTANT = 0x41727101980L;
    private static int MY_KEY = 0x17860253; // random constant
    private static int ACTION_CONNECT = 0;
    private static int ACTION_ANNOUNCE = 1;
    private static int EVENT_NONE = 0;
    private static int EVENT_COMPLETED = 1;
    private static int EVENT_STARTED = 2;
    private static int EVENT_STOPPED = 3;
}

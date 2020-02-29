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

        this.url = url;
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

        //eg. "udp://tracker.com:8080"
        int lenPrefix = 6; // "discard udp://"
        int portColon = url.lastIndexOf(":");
        String strAddr = url.substring(lenPrefix, portColon);
        String strPort = url.substring(portColon + 1);

        InetAddress address = InetAddress.getByName(strAddr);
        int port = Integer.parseInt(strPort);

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
        byte[] connectBuffer = ByteBuffer.allocate(16).putLong(MAGIC_CONSTANT).putInt(ACTION_CONNECT).putInt(transactionID).array();
        DatagramPacket connectPacket = new DatagramPacket(connectBuffer, connectBuffer.length, address, port);
        socket.send(connectPacket);

        /* Receive response */
        byte[] receiveBuf = new byte[16];
        DatagramPacket responsePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        socket.receive(responsePacket);
        ByteBuffer buffer = ByteBuffer.wrap(receiveBuf);
        if (buffer.getInt() != ACTION_CONNECT) {
            throw new IOException("Bad UDP Response: Did not receive CONNECT action.");
        }
        if (buffer.getInt() != transactionID) {
            throw new IOException("Bad UDP Response: Did not receive correct TRANSACTION_ID.");
        }

        return buffer.getLong(); // return connection ID
    }

    private void sendAnnounceRequest(DatagramSocket socket, InetAddress address, int port, long connectionID, int event)
            throws IOException, BencodeException {
        /* Send announce */
        int transactionID = getRandomTransactionID();
        byte[] announceBuffer = ByteBuffer.allocate(98)
                .putLong(connectionID)
                .putInt(ACTION_ANNOUNCE)
                .putInt(transactionID)
                .put(infoHash)
                .put(coordinator.getMyPeerID())
                .putLong(coordinator.getDownloaded())
                .putLong(coordinator.getLeft())
                .putLong(coordinator.getUploaded())
                .putInt(event)
                .putInt(0) // address DEFAULT 0
                .putInt(MY_KEY)
                .putInt(-1) // num_want DEFAULT -1
                .putShort((short) this.port)
                .array();
        DatagramPacket announcePacket = new DatagramPacket(announceBuffer, announceBuffer.length, address, port);
        socket.send(announcePacket);

        if (event == EVENT_STARTED || event == EVENT_NONE) {
            /* Receive response */
            byte[] receiveBuf = new byte[1024]; // large enough to hold peers
            DatagramPacket responsePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
            socket.receive(responsePacket);
            decodePeers(receiveBuf, responsePacket.getLength(), transactionID);
        }
    }

    /*
    Offset      Size            Name            Value
    0           32-bit integer  action          1 // announce
    4           32-bit integer  transaction_id
    8           32-bit integer  interval
    12          32-bit integer  leechers
    16          32-bit integer  seeders
    20 + 6 * n  32-bit integer  IP address
    24 + 6 * n  16-bit integer  TCP port
    20 + 6 * N
     */

    void decodePeers(byte[] receiveBuf, int receiveLength, int transactionID) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(receiveBuf);

        if (buffer.getInt() != ACTION_ANNOUNCE) {
            throw new IOException("Bad UDP Response: Did not receive ANNOUNCE action.");
        }
        if (buffer.getInt() != transactionID) {
            throw new IOException("Bad UDP Response: Did not receive correct TRANSACTION_ID.");
        }

        // Ignore these response parameters
        buffer.getInt(); // ignore interval
        buffer.getInt(); // ignore leechers
        buffer.getInt(); // ignore seeders

        int lenPeers = (receiveLength - 20) / 6;
        for (int peerNum = 0; peerNum < lenPeers; peerNum++) {
            byte[] addrIP = new byte[4];
            buffer.get(addrIP);
            // Bitwise AND to mask possible negative return on port
            int port = buffer.getShort() & 0xffff;
            peers.add(new PeerAddress(addrIP, port));
        }
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

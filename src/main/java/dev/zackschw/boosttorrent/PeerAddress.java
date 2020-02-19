package dev.zackschw.boosttorrent;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PeerAddress {
    private final InetAddress address;
    private final int port;

    public PeerAddress(String ip, int port) throws UnknownHostException {
        this.address = InetAddress.getByName(ip);
        this.port = port;
    }

    public PeerAddress(byte[] ip, int port) throws UnknownHostException {
        this.address = InetAddress.getByAddress(ip);
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}

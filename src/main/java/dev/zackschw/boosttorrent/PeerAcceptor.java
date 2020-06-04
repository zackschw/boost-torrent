package dev.zackschw.boosttorrent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Accepts new peers
 */
public class PeerAcceptor {
    private final PeerCoordinator coordinator;
    private final MetadataInfo meta;
    private final byte[] myPeerID;

    private boolean stop;
    private ServerSocket sock;

    public PeerAcceptor(PeerCoordinator coordinator, MetadataInfo meta, byte[] myPeerID) {
        this.coordinator = coordinator;
        this.meta = meta;
        this.myPeerID = myPeerID;

        stop = false;
    }

    /**
     * Opens a server socket to listen for connections in a new thread.
     * @return the port the server socket is listening on.
     * @throws IOException if the socket cannot bound to a port.
     */
    public int runListener() throws IOException {
        sock = new ServerSocket(0);

        Thread t = new Thread(this::listenForPeers);
        t.start();


        return sock.getLocalPort();
    }

    /**
     * Returns the port the server socket is listening on.
     */
    public int getLocalPort() {
        return sock.getLocalPort();
    }

    public void disconnect() {
        stop = true;
        try {
            sock.close();
        } catch (IOException ignore) {
        }
    }

    private void listenForPeers() {
        try {
            while (!stop ) {
                /* Accept connection */
                Socket newPeerSock = sock.accept();

                /* Add to peers */
                Peer peer = new Peer(newPeerSock, meta, myPeerID);
                coordinator.addPeer(peer);

            }
        } catch (IOException ignored) {
            // probably finished downloading and closing the socket
        }
    }
}

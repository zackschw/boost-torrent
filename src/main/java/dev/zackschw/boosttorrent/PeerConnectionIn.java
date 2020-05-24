package dev.zackschw.boosttorrent;

import java.io.DataInputStream;
import java.io.IOException;

public class PeerConnectionIn {
    private final Peer peer;
    private final DataInputStream din;

    PeerConnectionIn (Peer peer, DataInputStream din) {
        this.peer = peer;
        this.din = din;
    }

    void disconnect() {
        // TODO
    }

    void run() {
        try {
            while(true) {
                PeerState state = peer.getState();

                /* Read length */
                int len = din.readInt();

                if (len < 0) {
                    throw new IOException("Received unexpected length of message " + len);
                } else if (len == 0) {
                    // keep alive message
                    continue;
                }

                /* Read type */
                byte id = din.readByte();

                /* Read rest of message and alter state based on type received */
                switch (id) {
                    case Message.CHOKE:
                        break;
                    case Message.UNCHOKE:
                        break;
                    case Message.INTERESTED:
                        break;
                    case Message.UNINTERESTED:
                        break;
                    case Message.HAVE:
                        break;
                    case Message.BITFIELD:
                        break;
                    case Message.REQUEST:
                        break;
                    case Message.PIECE:
                        break;
                    case Message.CANCEL:
                        break;
                    default:
                        throw new IOException("Received unexpected message type " + id);
                }
            }

        } catch (IOException ignored) {

        } finally {
            disconnect();
        }
    }
}

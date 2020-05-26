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
                int index, begin, length;
                byte[] block;
                switch (id) {
                    case Message.CHOKE:
                        state.onChokeMessage(true);
                        break;
                    case Message.UNCHOKE:
                        state.onChokeMessage(false);
                        break;
                    case Message.INTERESTED:
                        state.onInterestMessage(true);
                        break;
                    case Message.NOTINTERESTED:
                        state.onInterestMessage(false);
                        break;
                    case Message.HAVE:
                        index = din.readInt();
                        state.onHaveMessage(index);
                        break;
                    case Message.BITFIELD:
                        block = new byte[len-1];
                        din.readFully(block);
                        state.onBitfieldMessage(block);
                        break;
                    case Message.REQUEST:
                        index = din.readInt();
                        begin = din.readInt();
                        length = din.readInt();
                        state.onRequestMessage(index, begin, length);
                        break;
                    case Message.PIECE:
                        block = new byte[len-9];
                        index = din.readInt();
                        begin = din.readInt();
                        din.readFully(block);
                        state.onPieceMessage(index, begin, block);
                        break;
                    case Message.CANCEL:
                        index = din.readInt();
                        begin = din.readInt();
                        length = din.readInt();
                        state.onCancelMessage(index, begin, length);
                        break;
                    default:
                        throw new IOException("Received unexpected message type " + id);
                }
            }

        } catch (IOException ignored) {

        } catch (Throwable t) {
            System.out.println("Fatal exception: " + t);
        } finally {
            disconnect();
        }
    }
}

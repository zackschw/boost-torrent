package dev.zackschw.boosttorrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class PeerConnectionIn {
    private final Peer peer;
    private final DataInputStream din;
    private boolean stop;

    PeerConnectionIn (Peer peer, DataInputStream din) {
        this.peer = peer;
        this.din = din;
        stop = false;
    }

    void disconnect() {
        stop = true;
    }

    void run() {
        try {
            while (!stop) {
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
                        index = din.readInt();
                        begin = din.readInt();
                        length = len-9;

                        Piece piece = state.getWorkingPiece(index);
                        if (piece != null) {
                            /* Read the block directly into the piece buffer */
                            block = piece.bytes;
                            din.readFully(block, begin, length);
                        } else {
                            /* Still need to consume the block */
                            block = new byte[length];
                            din.readFully(block);
                        }

                        state.incrementDownloaded(length);
                        state.onPieceMessage(piece, begin);
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

        } catch (SocketTimeoutException timeout) {
            peer.disconnect();
        } catch (IOException ignored) {
            // Likely socket closed
        } catch (Throwable t) {
            System.out.println("Fatal exception: " + t);
        } finally {
            disconnect();
        }
    }
}

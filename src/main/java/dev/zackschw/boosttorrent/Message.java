package dev.zackschw.boosttorrent;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object representing a message. Messages are sent to an output stream depending on their type.
 */
public class Message {
    final static byte CHOKE = 0;
    final static byte UNCHOKE = 1;
    final static byte INTERESTED = 2;
    final static byte NOTINTERESTED = 3;
    final static byte HAVE = 4;
    final static byte BITFIELD = 5;
    final static byte REQUEST = 6;
    final static byte PIECE = 7;
    final static byte CANCEL = 8;

    /**
     * Sending keep-alive message not implemented due to non-seeding bittorrent implementation
     */

    /**
     * Sends a Choke message.
     * @param dout data output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendChoke(DataOutputStream dout) throws IOException {
        //<len=1><id=1>
        dout.writeInt(1);
        dout.write(CHOKE & 0xFF);
    }

    /**
     * Sends an Unchoke message.
     * @param dout data output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendUnchoke(DataOutputStream dout) throws IOException {
        //<len=1><id=1>
        dout.writeInt(1);
        dout.write(UNCHOKE & 0xFF);
    }

    /**
     * Sends an Interested message.
     * @param dout data output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendInterested(DataOutputStream dout) throws IOException {
        //<len=1><id=2>
        dout.writeInt(1);
        dout.write(INTERESTED & 0xFF);
    }

    /**
     * Sends a Not Interested message.
     * @param dout data output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendNotInterested(DataOutputStream dout) throws IOException {
        //<len=1><id=3>
        dout.writeInt(1);
        dout.write(NOTINTERESTED & 0xFF);
    }

    /**
     * Sends a Have message.
     * @param dout data output stream to send to
     * @param pieceIndex zero-based index of a piece that has just been successfully downloaded and verified via hash
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendHave(DataOutputStream dout, int pieceIndex) throws IOException {
        //<len=5><id=4><piece index>
        dout.writeInt(5);
        dout.write(HAVE & 0xFF);
        dout.writeInt(pieceIndex);
    }

    /**
     * Sends a Bitfield message.
     * @param dout data output stream to send to
     * @param bitfield bitfield representing the pieces that have been successfully downloaded
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendBitfield(DataOutputStream dout, byte[] bitfield) throws IOException {
        //<len=1+bitfield.length><id=5><bitfield>
        dout.writeInt(1 + bitfield.length);
        dout.write(BITFIELD & 0xFF);
        dout.write(bitfield);
    }

    /**
     * Sends a Request message.
     * @param dout data output stream to send to
     * @param index the zero-based piece index
     * @param begin the zero-based byte offset within the piece
     * @param length the requested length
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendRequest(DataOutputStream dout, int index, int begin, int length) throws IOException {
        //<len=13><id=6><index><begin><length>
        dout.writeInt(13);
        dout.write(REQUEST & 0xFF);
        dout.writeInt(index);
        dout.writeInt(begin);
        dout.writeInt(length);
    }

    /**
     * Sends a Piece message.
     * @param dout data output stream to send to
     * @param index the zero-based piece index
     * @param begin the zero-based byte offset within the piece
     * @param block the block of data, which is a subset of the piece specified by index
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendPiece(DataOutputStream dout, int index, int begin, byte[] block) throws IOException {
        //<len=9+block.length><id=7><index><begin><block>
        dout.writeInt(9 + block.length);
        dout.write(PIECE & 0xFF);
        dout.writeInt(index);
        dout.writeInt(begin);
        dout.write(block);
    }

    /**
     * Sends a Cancel message.
     * @param dout data output stream to send to
     * @param index the zero-based piece index
     * @param begin the zero-based byte offset within the piece
     * @param length the requested length
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public static void sendCancel(DataOutputStream dout, int index, int begin, int length) throws IOException {
        //<len=13><id=8><index><begin><length>
        dout.writeInt(13);
        dout.write(CANCEL & 0xFF);
        dout.writeInt(index);
        dout.writeInt(begin);
        dout.writeInt(length);
    }
}
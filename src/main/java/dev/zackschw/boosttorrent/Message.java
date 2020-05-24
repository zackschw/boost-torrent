package dev.zackschw.boosttorrent;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Object representing a message. Messages are sent to an output stream depending on their type.
 */
public class Message {
    final static byte CHOKE = 0;
    final static byte UNCHOKE = 1;
    final static byte INTERESTED = 2;
    final static byte UNINTERESTED = 3;
    final static byte HAVE = 4;
    final static byte BITFIELD = 5;
    final static byte REQUEST = 6;
    final static byte PIECE = 7;
    final static byte CANCEL = 8;


    /**
     * Sends a Choke message.
     * @param out output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendChoke(OutputStream out) throws IOException {
        // TODO
    }

    /**
     * Sends an Unchoke message.
     * @param out output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendUnchoke(OutputStream out) throws IOException {
        // TODO
    }

    /**
     * Sends an Interested message.
     * @param out output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendInterested(OutputStream out) throws IOException {
        // TODO
    }

    /**
     * Sends a Not Interested message.
     * @param out output stream to send to
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendNotInterested(OutputStream out) throws IOException {
        // TODO
    }

    /**
     * Sends a Have message.
     * @param out output stream to send to
     * @param pieceIndex zero-based index of a piece that has just been successfully downloaded and verified via hash
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendHave(OutputStream out, int pieceIndex) throws IOException {
        // TODO
    }

    /**
     * Sends a Bitfield message.
     * @param out output stream to send to
     * @param bitfield bitfield representing the pieces that have been successfully downloaded
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendBitfield(OutputStream out, byte[] bitfield) throws IOException {
        // TODO
    }

    /**
     * Sends a Request message.
     * @param out output stream to send to
     * @param index the zero-based piece index
     * @param begin the zero-based byte offset within the piece
     * @param length the requested length
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendRequest(OutputStream out, int index, int begin, int length) throws IOException {
        // TODO
    }

    /**
     * Sends a Piece message.
     * @param out output stream to send to
     * @param index the zero-based piece index
     * @param begin the zero-based byte offset within the piece
     * @param block the block of data, which is a subset of the piece specified by index
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendPiece(OutputStream out, int index, int begin, byte[] block) throws IOException {
        // TODO
    }

    /**
     * Sends a Cancel message.
     * @param out output stream to send to
     * @param index the zero-based piece index
     * @param begin the zero-based byte offset within the piece
     * @param length the requested length
     * @throws IOException if an I/O error occurs writing to the output stream
     */
    public void sendCancel(OutputStream out, int index, int begin, int length) {
        // TODO
    }


}

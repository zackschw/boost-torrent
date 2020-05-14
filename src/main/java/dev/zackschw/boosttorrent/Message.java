package dev.zackschw.boosttorrent;

import java.io.IOException;
import java.io.OutputStream;

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

    private final byte type;

    private int piece; // used for HAVE, REQUEST, PIECE, CANCEL
    private int begin; // used for REQUEST, PIECE, CANCEL
    private int len; // used for REQUEST, PIECE, CANCEL
    private byte[] data; // used for BITFIELD, PIECE
    private int dataOffset; // used for PIECE
    private int datLen; // used for PIECE

    /**
     * Object to hold Message fields. Used to set all fields of the respective Message type before sending the message
     * to a peer.
     * @param type one of the Message types defined in this class.
     */
    public Message(byte type) {
        this.type = type;
    }

    void sendMessage(OutputStream out) throws IOException {
        int datalen = 0;

        /* TODO Set datalen based on type */

        /* TODO Write message to output stream */

    }

    /**
     * Used for HAVE, REQUEST, PIECE, CANCEL
     */
    void setPiece(int piece) {
        this.piece = piece;
    }

    /**
     * Used for REQUEST, PIECE, CANCEL
     */
    void setBegin(int begin) {
        this.begin = begin;
    }

    /**
     * Used for REQUEST, PIECE, CANCEL
     */
    void setLen(int len) {
        this.len = len;
    }

    /**
     * Used for BITFIELD, PIECE.
     * Pass the full bitfield byte array or piece byte array
     */
    void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Used for PIECE.
     * Offset into the piece data
     */
    void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    /**
     * Used for PIECE
     * Len of the piece data
     */
    void setDatLen(int datLen) {
        this.datLen = datLen;
    }
}

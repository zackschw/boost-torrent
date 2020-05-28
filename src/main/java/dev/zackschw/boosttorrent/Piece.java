package dev.zackschw.boosttorrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Piece {
    private final int index;
    private final int length;
    private final byte[] hash;

    final byte[] bytes; // bytes received from a peer

    /**
     * Piece objects are specified by an index and a length.
     * Piece.bytes stores the bytes of the Piece received by a peer.
     * @param index zero-based piece index
     * @param length length of the piece
     * @param hash 20-byte SHA1 hash of the piece, from the meta file
     */
    public Piece(int index, int length, byte[] hash) {
        this.index = index;
        this.length = length;
        this.hash = hash;

        bytes = new byte[length];
    }

    /**
     * Computes the SHA-1 hash of the piece bytes, and compares it to the hash from the meta file.
     * @return true if the hashes are the same, otherwise false
     */
    public boolean checkHash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

        return Arrays.equals(md.digest(bytes), hash);
    }
}

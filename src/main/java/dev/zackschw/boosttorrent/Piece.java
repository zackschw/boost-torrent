package dev.zackschw.boosttorrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Piece {
    final int index;
    final int length;
    final byte[] bytes; // bytes received from a peer
    private final byte[] hash;
    private final Bitvector receivedBlocks;

    private int count;

    /**
     * Piece objects are specified by an index and a length.
     * Piece.bytes stores the bytes of the Piece received by a peer.
     * Piece objects keep track of which blocks have been received via calls to onReceivedBlock().
     * A client implementation can check if all blocks have been received via receivedAllBlocks().
     * @param index zero-based piece index
     * @param length length of the piece
     * @param hash 20-byte SHA1 hash of the piece, from the meta file
     */
    public Piece(int index, int length, byte[] hash) {
        this.index = index;
        this.length = length;
        this.hash = hash;

        count = 0;

        bytes = new byte[length];
        receivedBlocks = new Bitvector(length/BLOCK_LENGTH + (length % BLOCK_LENGTH == 0 ? 0 : 1));
    }

    /**
     * On receiving a block from a peer.
     * @param begin the offset into the piece for the block received
     */
    public void onReceivedBlock(int begin) {
        receivedBlocks.setBit(begin/BLOCK_LENGTH);
        count++;
    }

    /**
     * @return the number of blocks already received in this piece, via onReceivedBlock()
     */
    public int getNumReceivedBlocks() {
        return count;
    }

    public boolean receivedAllBlocks() {
        return receivedBlocks.isComplete();
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

    public static int BLOCK_LENGTH = 1<<14;
}

package dev.zackschw.boosttorrent;

public class Bitfield {
    private final int size;
    private final byte[] bytes;

    /**
     * Creates an empty (all zeroes) bitfield with number of bits equal to size
     * @param size number of bits of the bitfield
     */
    public Bitfield(int size) {
        this.size = size;

        int numBytes = size/8 + (size % 8 == 0 ? 0 : 1);
        this.bytes = new byte[numBytes];
    }

    /**
     * Creates a bitfield with the bitmap and number of bits equal to size
     * @param size number of bits of the bitfield
     * @param bitmap pre-populated bitfield
     * @throws IllegalArgumentException if the size does not correspond with the number of bytes in the bitmap
     */
    public Bitfield(int size, byte[] bitmap) {
        this.size = size;
        this.bytes = bitmap;

        int numBytes = size/8 + (size % 8 == 0? 0 : 1);
        if (bitmap.length != numBytes)
            throw new IllegalArgumentException("Tried to create bitmap of size " + size + " and number of bytes " + bitmap.length);
    }

    /**
     * Sets bit index to 1
     * @param bit zero-indexed bit to set
     */
    public void setBit(int bit) {
        // TODO
    }

    /**
     * Checks whether the bit index is set to 1
     * @param bit zero-indexed bit to check
     * @return true if bit index is 1, otherwise false
     */
    public boolean isSet(int bit) {
        // TODO
        return false;
    }
}

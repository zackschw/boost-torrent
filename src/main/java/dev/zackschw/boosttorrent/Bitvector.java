package dev.zackschw.boosttorrent;

public class Bitvector {
    private final int size;
    private final byte[] bytes;

    /**
     * Creates an empty (all zeroes) bitfield with number of bits equal to size
     * @param size number of bits of the bitfield
     */
    public Bitvector(int size) {
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
    public Bitvector(int size, byte[] bitmap) {
        this.size = size;
        this.bytes = bitmap;

        int numBytes = size/8 + (size % 8 == 0? 0 : 1);
        if (bitmap.length != numBytes)
            throw new IllegalArgumentException("Tried to create bitmap of size " + size + " and number of bytes " + bitmap.length);
    }

    /**
     * Sets bit index to 1
     * @param bit zero-indexed bit to set
     * @throws IllegalArgumentException if the bit is out of bounds of the size of the bitvector
     */
    public void setBit(int bit) {
        // TODO
    }

    /**
     * Sets bit index to 0
     * @param bit zero-indexed bit to unset
     * @throws IllegalArgumentException if the bit is out of bounds of the size of the bitvector
     */
    public void unsetBit(int bit) {
        // TODO
    }

    /**
     * Checks whether the bit index is set to 1
     * @param bit zero-indexed bit to check
     * @return true if bit index is 1, otherwise false
     * @throws IllegalArgumentException if the bit is out of bounds of the size of the bitvector
     */
    public boolean isSet(int bit) {
        // TODO
        return false;
    }

    /**
     * Checks whether all bits are set to 1
     * @return true if the bitvector is complete, otherwise false
     */
    public boolean isComplete() {
        // TODO
        return false;
    }

    /**
     * Checks whether all bits are set to 0
     * @return true if the bitvector is empty, otherwise false
     */
    public boolean isEmpty() {
        // TODO
        return false;
    }

    /**
     * Returns the bitvector as a byte array.
     */
    public byte[] toByteArray() {
        return bytes;
    }
}

package dev.zackschw.boosttorrent;

/**
 * Bitvectors represent a bit array, where individual bits can be set and unset.
 * The first bit is index zero and starts on the far left of the bitvector, and each bit to the right represents an
 * increment of the bit index.
 * The bitvector size is in number of bits and is immutable.
 * All Bitvector operations are thread safe.
 */
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
     * @return size of the bitvector
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets bit index to 1
     * @param bit zero-indexed bit to set
     * @throws IllegalArgumentException if the bit is out of bounds of the size of the bitvector
     */
    public void setBit(int bit) {
        /* Check bounds */
        if (bit >= size || bit < 0) {
            throw new IllegalArgumentException("Cannot set bit " + bit + " in bitvector of size " + size);
        }

        int byteIndex = bit / 8;
        int bitPos = 7 - (bit % 8);

        synchronized (this) {
            bytes[byteIndex] |= 1 << bitPos;
        }
    }

    /**
     * Sets bit index to 0
     * @param bit zero-indexed bit to unset
     * @throws IllegalArgumentException if the bit is out of bounds of the size of the bitvector
     */
    public void unsetBit(int bit) {
        /* Check bounds */
        if (bit >= size || bit < 0) {
            throw new IllegalArgumentException("Cannot unset bit " + bit + " in bitvector of size " + size);
        }

        int byteIndex = bit / 8;
        int bitPos = 7 - (bit % 8);

        synchronized (this) {
            bytes[byteIndex] &= ~(1 << bitPos);
        }
    }

    /**
     * Checks whether the bit index is set to 1
     * @param bit zero-indexed bit to check
     * @return true if bit index is 1, otherwise false
     * @throws IllegalArgumentException if the bit is out of bounds of the size of the bitvector
     */
    public boolean isSet(int bit) {
        /* Check bounds */
        if (bit >= size || bit < 0) {
            throw new IllegalArgumentException("Cannot check bit " + bit + " in bitvector of size " + size);
        }

        int byteIndex = bit / 8;
        int bitPos = 7 - (bit % 8);

        synchronized (this) {
            return (bytes[byteIndex] & (1 << bitPos)) != 0;
        }
    }

    /**
     * fast isSet() method that skips synchronization and bounds checking.
     */
    private boolean fastIsSet(int bit) {
        int byteIndex = bit/8; // from the left
        int bitPos = 7 - (bit % 8); // from the right
        return (bytes[byteIndex] & (1 << bitPos)) != 0;
    }

    /**
     * Checks whether all bits are set to 1
     * @return true if the bitvector is complete, otherwise false
     */
    public boolean isComplete() {
        int numFullBytes = size/8;

        synchronized (this) {
            for (int i=0; i < numFullBytes; i++) {
                if (bytes[i] != (byte) 0xff)
                    return false;
            }

            int remaining = size % 8;
            for (int i=0; i < remaining; i++) {
                if (!fastIsSet(numFullBytes*8 + i))
                    return false;
            }
        }

        return true;
    }

    /**
     * Checks whether all bits are set to 0
     * @return true if the bitvector is empty, otherwise false
     */
    public boolean isEmpty() {
        synchronized (this) {
            for (byte b : bytes) {
                if (b != 0)
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns the bitvector as a byte array.
     */
    public byte[] toByteArray() {
        return bytes;
    }
}

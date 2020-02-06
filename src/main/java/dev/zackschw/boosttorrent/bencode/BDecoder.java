package dev.zackschw.boosttorrent.bencode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BDecoder implements Closeable {

    private final PushbackInputStream in;

    /**
     * Creates a BencodeReader instance from an underlying InputStream
     * @param in The underlying InputStream to read from
     */
    public BDecoder(InputStream in) {
        this.in = new PushbackInputStream(in);
    }


    /**
     * Closes the BDecoder (but not the underlying InputStream)
     * @throws IOException if IOException occurs when closing
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Reads one byte of the input stream
     * @throws IOException if IOException occurs during read
     */
    private int readOne() throws IOException {
        int t = in.read();
        if (t == -1) {
            throw new EOFException();
        }

        return t;
    }

    /**
     * Peeks the next byte of the input stream
     * @throws IOException if IOException occurs during read
     */
    private int peek() throws IOException {
        // TODO
        return 0;
    }


    /**
     * Reads a Bencoded long from the input stream
     * @return the long value wrapped in a BValue
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public BValue readLong() throws IOException, BencodeException {
        int firstByte = readOne();
        long ret = 0;

        if (firstByte != 'i') {
            throw new BencodeException("Unexpected " + firstByte + " leading Bencoded integer, expected 'i'");
        }

        boolean neg=false, readNum=false;

        while (!readNum) {
            // TODO read long
        }

        return neg ? new BValue(-ret) : new BValue(ret);
    }


    /**
     * Reads length (positive int) up until ':' from input stream
     * @return length of string
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    private int readStringLen() throws IOException, BencodeException {
        int ret=0;
        boolean readNum = false;

        while (!readNum) {
            // TODO read positive int
        }

        return ret;
    }

    /**
     * Reads a Bencoded string from the input stream
     * @return the string value as a byte array, wrapped in a BValue
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public  BValue readStringAsBytes() throws IOException, BencodeException {
        int len = readStringLen();
        byte[] bytes = new byte[len];

        // TODO read bytes

        return new BValue(bytes);
    }


    /**
     * Reads an entire Bencoded list from the input stream
     * @return A List, wrapped in a BencodeValue, of BencodeValues that were read from other Bencoded items.
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public BValue readList() throws IOException, BencodeException {
        int firstByte = readOne();
        if (firstByte != 'l') {
            throw new BencodeException("Unexpected " + firstByte + " leading Bencoded list, expected 'l'");
        }

        List<BValue> ret = new ArrayList<>();

        while (peek() != 'e') {
            // TODO add items to list
        }

        readOne(); // read the 'e' that was peeked

        return new BValue(ret);
    }


    /**
     * Reads an entire Bencoded dictionary from the input stream
     * @return A Map, wrapped in a BencodeValue, of (String,Bencode) pairs that were read from other Bencoded items
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public BValue readDict() throws IOException, BencodeException {
        int firstByte = readOne();
        if (firstByte != 'd') {
            throw new BencodeException("Unexpected " + firstByte + " leading Bencoded dictionary, expected 'd'");
        }

        Map<String, BValue> ret = new HashMap<>();

        // TODO read (String, BValue) pairs and add to map

        return new BValue(ret);
    }


    /**
     * Reads a BValue from the input stream
     * @throws IOException if IOException occurs during read (including EOFException)
     * @throws BencodeException if any value read is not properly Bencoded
     */
    public BValue read(InputStream in) throws IOException, BencodeException {
        int t = peek();

        switch(t) {
            // TODO read various bencoded values
        }
        return null;
    }
}

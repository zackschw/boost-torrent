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
     * @return next byte from the input stream
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
     * @return a copy of the next byte from the input stream
     * @throws IOException if IOException occurs during read
     */
    private int peek() throws IOException {
        int t = readOne();
        in.unread(t);

        return t;
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
     * Reads length (positive int) up until ':' from input stream (discards ':' character)
     * @return length of string
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    private int readStringLen() throws IOException, BencodeException {
        boolean readNum = false;
        String rawInt = "";

        while (!readNum) {
            //read the char off the front of the stream
            int b = readOne();

            //read chars off the stream until reaching the colon or a non-int char
            if (b == ':') {
                readNum = true;
            } else if (b - '0' >= 0 && b - '9' <= 0) {
                rawInt += (char) b;
            } else {
                throw new BencodeException("Found illegal character in string length field!");
            }
        }

        return Integer.parseInt(rawInt);
        //if we don't like this implementation, we can do the following:
        //1. create this method:
            //private int countBytesUntilChar(char c) {
                //peek bytes and increment counter until peekedByte == c
        //2. call the method on the encoded string to read how many digits the int is
        //3. loop through each digit of the int
            //while lenInt-- > 1
                //result += 10^lenInt-- * (int)char
    }

    /**
     * Reads a Bencoded string from the input stream
     * @return the string value as a byte array, wrapped in a BValue
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public  BValue readStringAsBytes() throws IOException, BencodeException {
        //calling readStringLen will discard from stream everything up until the start of the actual string
        int len = readStringLen();
        byte[] bytes = new byte[len];

        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) readOne();
        }

        //TODO can we verify that the string was the right length and not encoded erroneously?
        //eg. if the string was 5:sample can we identify that and throw an error?
        //if we have 7:sample can we do the same?

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

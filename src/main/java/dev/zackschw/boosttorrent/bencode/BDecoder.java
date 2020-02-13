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
     * Reads an encoded int that is end-delimited by input character c; handles negatives and discards end char
     * @return the decoded integer value
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is illegal
     */
    private int readIntUntilChar(char c) throws IOException, BencodeException {
        boolean readNum = false, neg = false;
        String rawInt = "";

        if (peek() == '-') {
            neg = true;
            readOne();
        }

        while (!readNum) {
            //read the char off the front of the stream
            char b = (char) readOne();

            //read chars off the stream until reaching the colon or a non-int char
            if (b == c) {
                readNum = true;
            }
            else if (b - '0' >= 0 && b - '9' <= 0) {
                rawInt += b;
            }
            else {
                throw new BencodeException("Found illegal character " + b + " in integer!");
            }
        }

        int res = Integer.parseInt(rawInt);

        if (res == 0 && neg) {
            throw new BencodeException("Returned integer value of negative 0 is not a legal Bencode value!");
        }

        return neg ? -res : res;
    }

    /**
     * Reads a Bencoded long from the input stream
     * @return the long value wrapped in a BValue
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public BValue readLong() throws IOException, BencodeException {
        int firstByte = readOne();

        if (firstByte != 'i') {
            throw new BencodeException("Unexpected " + firstByte + " leading Bencoded integer, expected 'i'");
        }

        long ret = readIntUntilChar('e');

        return new BValue(ret);
    }

    /**
     * Reads a Bencoded string from the input stream
     * @return the string value as a byte array, wrapped in a BValue
     * @throws IOException if IOException occurs during read
     * @throws BencodeException if value is not properly Bencoded
     */
    public BValue readStringAsBytes() throws IOException, BencodeException {
        //calling readIntUntilChar will discard from stream everything up until the start of the actual string
        int len = readIntUntilChar(':');
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
            ret.add(read(in));
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

        while (peek() != 'e') {
            ret.put(readStringAsBytes().getString(), read(in));
        }

        readOne(); //read the 'e' that was peeked

        return new BValue(ret);
    }


    /**
     * Reads a BValue from the input stream
     * @throws IOException if IOException occurs during read (including EOFException)
     * @throws BencodeException if any value read is not properly Bencoded
     */
    public BValue read(InputStream in) throws IOException, BencodeException {
        int t = peek();
        BValue result;

        //int will start with i
        if (t == 'i') {
            result = readLong();
        }

        //string will start with an int 0-9
        else if (t - '0' >= 0 && t - '9' <= 0) {
            result = readStringAsBytes();
        }

        //list will start with l
        else if (t == 'l') {
            result = readList();
        }

        //dict will start with d
        else if (t == 'd') {
            result = readDict();
        }

        //unsupported types will return an error
        else {
            throw new BencodeException("Bad encoding: First byte " + t + " doesn't represent supported type.");
        }

        return result;
    }
}


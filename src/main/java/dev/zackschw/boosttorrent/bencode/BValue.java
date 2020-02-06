package dev.zackschw.boosttorrent.bencode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BValue {
    private final Object value;

    public BValue(Object value) {
        this.value = value;
    }

    /**
     * @return This BencodeValue as an Object
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @return This BencodeValue as a String, interpreted as UTF-8.
     */
    public String getString() throws BencodeException {
        try {
            return new String(this.getBytes(), StandardCharsets.UTF_8);
        } catch (ClassCastException cce) {
            throw new BencodeException(cce.toString());
        }
    }

    /**
     * @return This BencodeValue as a byte[].
     * @throws BencodeException If the value is not a byte[].
     */
    public byte[] getBytes() throws BencodeException {
        try {
            return (byte[]) this.value;
        } catch (ClassCastException cce) {
            throw new BencodeException(cce.toString());
        }
    }

    /**
     * @return This BencodeValue as a short.
     * @throws BencodeException If the value cannot be represented by a short.
     */
    public short getShort() throws BencodeException {
        try {
            return (short) this.getLong(); // value was decoded as a long
        } catch (ClassCastException cce) {
            throw new BencodeException(cce.toString());
        }
    }

    /**
     * @return This BencodeValue as an int.
     * @throws BencodeException If the value cannot be represented by an int.
     */
    public int getInt() throws BencodeException {
        try {
            return (int) this.getLong(); // value was decoded as a long
        } catch (ClassCastException cce) {
            throw new BencodeException(cce.toString());
        }
    }

    /**
     * @return This BencodeValue as a long.
     * @throws BencodeException If the value cannot be represented by a long.
     */
    public long getLong() throws BencodeException {
        try {
            return (long) this.value;
        } catch (ClassCastException cce) {
            throw new BencodeException(cce.toString());
        }
    }

    /**
     * @return This BencodeValue as a List of BencodeValues.
     * @throws BencodeException If the value is not an ArrayList
     */
    @SuppressWarnings("unchecked")
    public List<BValue> getList() throws BencodeException {
        if (this.value instanceof List) {
            return (List<BValue>) this.value;
        } else {
            throw new BencodeException("Excepted List<BencodeValue> !");
        }
    }

    /**
     * @return This BencodeValue as a Map of key:String, value:BencodeValue
     * @throws BencodeException If the value is not a HashMap
     */
    @SuppressWarnings("unchecked")
    public Map<String, BValue> getMap() throws BencodeException {
        if (this.value instanceof Map) {
            return (Map<String, BValue>) this.value;
        } else {
            throw new BencodeException("Expected Map<String, BEValue> !");
        }
    }
}

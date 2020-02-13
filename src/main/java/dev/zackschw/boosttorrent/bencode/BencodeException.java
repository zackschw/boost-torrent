package dev.zackschw.boosttorrent.bencode;

/**
 * Exception occurred during BDecoding
 */
public class BencodeException extends Exception {
    public BencodeException(String message, Object... args) {
        super(String.format(message, args));
    }
}

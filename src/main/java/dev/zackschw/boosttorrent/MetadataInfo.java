package dev.zackschw.boosttorrent;

import dev.zackschw.boosttorrent.bencode.BDecoder;
import dev.zackschw.boosttorrent.bencode.BValue;
import dev.zackschw.boosttorrent.bencode.BencodeException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class to hold all torrent information from the metadata file.
 */
public class MetadataInfo {
    private final byte[] infoHash;
    private final String announce;
    private final List<List<String>> announceList;
    private final int pieceLength;
    private final byte[][] pieceHashes; // byte array of HASH_LENGTH byte arrays, each entry in outer array is one piece
    private final String name; // filename
    private final List<MetaFile> files; // Single-file mode if list length is 1, multi-file mode if >1

    MetadataInfo(byte[] infoHash,
                    String announce,
                    List<List<String>> announceList,
                    int pieceLength,
                    byte[][] pieceHashes,
                    String name,
                    List<MetaFile> files) {
        this.infoHash = infoHash;
        this.announce = announce;
        this.announceList = announceList;
        this.pieceLength = pieceLength;
        this.pieceHashes = pieceHashes;
        this.name = name;
        this.files = files;
    }

    /**
     * Creates MetadataInfo by reading from and decoding the input stream.
     * @param in InputStream to read from.
     */
    public MetadataInfo(InputStream in) throws IOException, BencodeException {
        this(new BDecoder(in));
    }

    /**
     * Creates MetadataInfo from the given BDecoder.
     * @param be BDecoder to read from.
     */
    public MetadataInfo(BDecoder be) throws IOException, BencodeException {
        this(be.readDict().getMap());
    }

    /**
     * Creates MetadataInfo from the given dictionary of BValues
     * @param m Map of the dictionary of BValues from the metadata.
     */
    public MetadataInfo(Map<String, BValue> m) {
        // TODO getValues
        this.infoHash = null;
        this.announce = null;
        this.announceList = null;
        this.pieceLength = 0;
        this.pieceHashes = null;
        this.name = null;
        this.files = null;
    }

    private BValue getRequiredValue(Map<String, BValue> m, String key) throws BencodeException {
        BValue val = m.get(key);
        if (val == null)
            throw new BencodeException("Missing metadata key " + key);

        return val;
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public String getAnnounce() {
        return announce;
    }

    public List<List<String>> getAnnounceList() {
        return announceList;
    }

    public int getPieceLength() {
        return pieceLength;
    }

    public int getNumPieces() {
        return pieceHashes.length;
    }

    public boolean comparePieceHash(int index, byte[] fullPieceBytes) {
        return Arrays.equals(pieceHashes[index], hash(fullPieceBytes));
    }

    public String getName() {
        return name;
    }

    public List<MetaFile> getFiles() {
        return files;
    }

    public long getTotalFileBytes() {
        long totalFileBytes = 0;
        for (MetaFile file : files)
            totalFileBytes += file.getLength();

        return totalFileBytes;
    }

    public int getLastPieceLength() {
        return (int) (getTotalFileBytes() - (pieceHashes.length-1)*pieceLength);
    }

    private byte[] hash(byte[] input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

        return md.digest(input);
    }
}

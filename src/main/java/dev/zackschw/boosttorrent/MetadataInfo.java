package dev.zackschw.boosttorrent;

import dev.zackschw.boosttorrent.bencode.BDecoder;
import dev.zackschw.boosttorrent.bencode.BEncoder;
import dev.zackschw.boosttorrent.bencode.BValue;
import dev.zackschw.boosttorrent.bencode.BencodeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    public MetadataInfo(Map<String, BValue> m) throws BencodeException, IOException {
        Map<String, BValue> infoMap = getRequiredValue(m, "info").getMap();
        this.infoHash = getHashFromInfo(infoMap);
        this.announce = getRequiredValue(m, "announce").getString();
        this.announceList = getNestedListValues(m);
        this.pieceLength = (int) getRequiredValue(infoMap, "piece length").getLong();
        this.pieceHashes = getHashArray(infoMap, 20);
        this.name = getRequiredValue(infoMap, "name").getString();
        this.files = getMetaFiles(infoMap);
    }

    private BValue getRequiredValue(Map<String, BValue> m, String key) throws BencodeException {
        BValue val = m.get(key);
        if (val == null)
            throw new BencodeException("Missing metadata key " + key);

        return val;
    }

    private byte[] getHashFromInfo(Map<String, BValue> infoMap) throws BencodeException, IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        BEncoder.write(infoMap, o);
        return hash(o.toByteArray());
    }

    private List<List<String>> getNestedListValues(Map<String, BValue> m) throws BencodeException {
        List<BValue> outerList = getRequiredValue(m, "announce-list").getList();
        List<List<String>> returnList = new ArrayList<>();
        for (BValue outerListElem : outerList){
            List<BValue> announceList = outerListElem.getList();
            List<String> urlList = new ArrayList<>();
            for (BValue tracker : announceList) {
                urlList.add(tracker.getString());
            }
            returnList.add(urlList);
        }

        return returnList;
    }

    private List<MetaFile> getMetaFiles(Map<String, BValue> infoMap) throws BencodeException {
        List<MetaFile> returnList = new ArrayList<>();
        if (infoMap.containsKey("files")) {
            List<BValue> fileList = getRequiredValue(infoMap, "files").getList();
            for (BValue file : fileList) {
                Map<String, BValue> fileDict = file.getMap();
                List<BValue> filePath = getRequiredValue(fileDict, "path").getList();
                StringBuilder outPath = new StringBuilder(getRequiredValue(infoMap, "name").getString() + "/");
                for (BValue subDir : filePath) {
                    outPath.append(subDir.getString()).append("/");
                }
                //remove the last / from the outPath
                outPath.deleteCharAt(outPath.length() - 1);
                long fileLength = getRequiredValue(fileDict, "length").getLong();
                returnList.add(new MetaFile(outPath.toString(), fileLength));
            }
        }
        else {
            String filePath = getRequiredValue(infoMap, "name").getString();
            long fileLength = getRequiredValue(infoMap, "length").getLong();
            returnList.add(new MetaFile(filePath, fileLength));
        }

        return returnList;
    }

    public byte[][] getHashArray(Map<String, BValue> infoMap, int lenHash) throws BencodeException {
        byte[] allHashes = getRequiredValue(infoMap, "pieces").getBytes();
        byte[][] returnArray = new byte[allHashes.length / lenHash][];

        for (int pieceNum = 0; pieceNum < allHashes.length / lenHash; pieceNum++) {
            returnArray[pieceNum] = Arrays.copyOfRange(allHashes, pieceNum * lenHash, (pieceNum + 1) * lenHash);
        }

        return returnArray;
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
        //System.out.println(new String(input));
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }

        return md.digest(input);
    }
}

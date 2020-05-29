package dev.zackschw.boosttorrent;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Storage {
    private final MetadataInfo meta;

    private final Bitvector myBitfield;
    private final RandomAccessFile[] files;

    /**
     * Creates a Storage for writing pieces to, and reading pieces from.
     * @param meta torrent metadata info
     */
    public Storage(MetadataInfo meta) {
        this.meta = meta;

        myBitfield = new Bitvector(meta.getNumPieces());
        files = new RandomAccessFile[meta.getFiles().size()];
    }

    public Bitvector getMyBitfield() {
        return myBitfield;
    }

    /**
     * Creates RandomAccessFiles based on the files specified in the metadata info.
     * If the files already exist, the piece hashes are checked and set appropriately in the bitfield.
     * @throws IOException if an I/O error occurs when creating a file or reading from an existing file
     */
    public void createFiles() throws IOException {
        for (int i=0; i < files.length; i++) {
            MetaFile m = meta.getFiles().get(i);
            files[i] = new RandomAccessFile(m.getPath(), "rw");
            files[i].setLength(m.getLength());
        }
    }

    /**
     * Writes the piece to storage
     * @param piece the finished piece received from peers, with correct hash.
     */
    public void writePiece(Piece piece) {
        // TODO
    }

    /**
     * Reads the requested block from storage.
     * @param piece index of the piece
     * @param begin offset into the piece
     * @param length length of the requested block
     * @return byte array of requested length representing the block specified by piece index and begin offset.
     */
    public byte[] readBlock(int piece, int begin, int length) {
        // TODO
        return null;
    }
}

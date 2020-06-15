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
        long pieceStartPos = piece.index * meta.getPieceLength();
        long pieceEndPos = pieceStartPos + piece.length;

        long fileStartPos = 0;
        long fileEndPos = 0;
        int i = 0;
        try {
            synchronized (this) {
                /* Find the file where the piece begins */
                do {
                    fileStartPos = fileEndPos;
                    fileEndPos += meta.getFiles().get(i).getLength();
                    i++;
                } while (!(pieceStartPos >= fileStartPos && pieceStartPos < fileEndPos));

                /* Write all the bytes we can to this file, then go to the next and repeat */
                int bytesWritten = 0;
                while (bytesWritten < piece.length) {
                    /* Write whichever is shorter: to end of file or to end of the piece */
                    long writeEndPos = Math.min(pieceEndPos, fileEndPos);
                    int bytesToWrite = (int) (writeEndPos - (pieceStartPos + bytesWritten));

                    /* Write */
                    files[i].seek(pieceStartPos + bytesWritten - fileStartPos);
                    files[i].write(piece.bytes, bytesWritten, bytesToWrite);
                    bytesWritten += bytesToWrite;

                    /* Next file */
                    i++;
                    fileStartPos = fileEndPos;
                    fileEndPos += meta.getFiles().get(i).getLength();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        byte[] pieceOut = new byte[length];

        long blockStartPos = piece * meta.getPieceLength() + begin;
        long blockEndPos = blockStartPos + length;

        long fileStartPos = 0;
        long fileEndPos = 0;
        int i = 0;

        try {
            synchronized (this) {
                /* Find the file where the block begins */
                do {
                    fileStartPos = fileEndPos;
                    fileEndPos += meta.getFiles().get(i).getLength();
                    i++;
                } while (!(blockStartPos >= fileStartPos && blockStartPos < fileEndPos));

                /* Read all the bytes we can from this file, then go to the next and repeat */
                int bytesRead = 0;
                while (bytesRead < length) {
                    /* Read whichever is shorter: to end of file or to end of the block */
                    long readEndPos = Math.min(blockEndPos, fileEndPos);
                    int bytesToRead = (int) (readEndPos - (blockStartPos + bytesRead));

                    /* Read */
                    files[i].seek(blockStartPos + bytesRead - fileStartPos);
                    files[i].read(pieceOut, bytesRead, bytesToRead);
                    bytesRead += bytesToRead;

                    /* Next file */
                    i++;
                    fileStartPos = fileEndPos;
                    fileEndPos += meta.getFiles().get(i).getLength();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return pieceOut;
    }
}

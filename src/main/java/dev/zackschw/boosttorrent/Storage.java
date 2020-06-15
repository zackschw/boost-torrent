package dev.zackschw.boosttorrent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
        boolean filesExist = false;

        for (int i=0; i < files.length; i++) {
            MetaFile m = meta.getFiles().get(i);
            File f = new File(m.getPath());

            /* If the file does not exist, create parent directories as needed */
            if (!f.exists()) {
                /* Create parent directories as needed */
                if (m.getPath().contains("/")) {
                    f.getParentFile().mkdirs();
                }
            } else {
                filesExist = true;
            }

            files[i] = new RandomAccessFile(m.getPath(), "rw");
            files[i].setLength(m.getLength());
        }

        if (filesExist) {
            checkPieceHashes();
        }
    }

    /**
     * Checks the files for any pieces that are already complete and satisfy the hash check.
     * The bitvector is updated appropriately.
     */
    private void checkPieceHashes() {
        /* First n-1 pieces */
        for (int i=0; i < meta.getNumPieces() - 1; i++) {
            /* Read block */
            byte[] pieceBytes = readBlock(i, 0, meta.getPieceLength());

            /* Check hash */
            if (Arrays.equals(hash(pieceBytes), meta.getPieceHash(i))) {
                myBitfield.setBit(i);
            }

        }

        /* Last piece */
        byte[] pieceBytes = readBlock(meta.getNumPieces()-1, 0, meta.getLastPieceLength());
        if (Arrays.equals(hash(pieceBytes), meta.getPieceHash(meta.getNumPieces()-1))) {
            myBitfield.setBit(meta.getNumPieces()-1);
        }
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

    /**
     * Closes all files.
     */
    public void closeAll() {
        for (RandomAccessFile file : files) {
            try {
                file.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Writes the piece to storage
     * @param piece the finished piece received from peers, with correct hash.
     */
    public void writePiece(Piece piece) {
        long pieceStartPos = piece.index * meta.getPieceLength();
        long pieceEndPos = pieceStartPos + piece.length;

        boostFileIO(pieceStartPos, pieceEndPos, piece.bytes, 'w');
    }

    /**
     * Reads the requested block from storage.
     * @param piece index of the piece
     * @param begin offset into the piece
     * @param length length of the requested block
     * @return byte array of requested length representing the block specified by piece index and begin offset.
     */
    public byte[] readBlock(int piece, int begin, int length) {
        long blockStartPos = piece * meta.getPieceLength() + begin;
        long blockEndPos = blockStartPos + length;

        byte[] pieceOut = new byte[length];

        boostFileIO(blockStartPos, blockEndPos, pieceOut, 'r');

        return pieceOut;
    }

    /**
     * Handles file input/output. Writes pieces across files (if needed) or reads blocks across files (if needed).
     * @param objectStartPos start position of the object to IO
     * @param objectEndPos end position of the object to IO
     * @param ioArray byte array to IO from/to
     * @param ioMode action to be performed [r = read object from array; w = write object to array]
     */
    private void boostFileIO (long objectStartPos, long objectEndPos, byte[] ioArray, char ioMode) {
        long length = objectEndPos - objectStartPos;

        long fileStartPos = 0;
        long fileEndPos = 0;
        int i = -1;

        try {
            synchronized (this) {
                /* Find the file where the block/piece begins */
                do {
                    i++;
                    fileStartPos = fileEndPos;
                    fileEndPos += meta.getFiles().get(i).getLength();
                } while (!(objectStartPos >= fileStartPos && objectStartPos < fileEndPos));

                /* Read/write all the bytes we can from this file, then go to the next and repeat */
                int bytesHandled = 0;
                while (bytesHandled < length) {
                    /* Read/write whichever is shorter: end of file or end of the block/piece */
                    long ioEndPos = Math.min(objectEndPos, fileEndPos);
                    int bytesToHandle = (int) (ioEndPos - (objectStartPos + bytesHandled));

                    /* Perform IO */
                    files[i].seek(objectStartPos + bytesHandled - fileStartPos);
                    if (ioMode == 'r') {
                        /* Read */
                        files[i].read(ioArray, bytesHandled, bytesToHandle);
                    }
                    else if (ioMode == 'w') {
                        /* Write */
                        files[i].write(ioArray, bytesHandled, bytesToHandle);
                    }
                    bytesHandled += bytesToHandle;

                    /* Next file */
                    if (++i < meta.getFiles().size()) {
                        fileStartPos = fileEndPos;
                        fileEndPos += meta.getFiles().get(i).getLength();
                    } else {
                        if (bytesHandled != length)
                            throw new RuntimeException("Storage: Handled " + bytesHandled + " bytes, Ran out of files to read/write to");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package dev.zackschw.boosttorrent;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

public class StorageTest {

    private MetadataInfo createTestMeta() throws Exception {
        /* Name: multiFile
         * File1: multiFile/file1  --  length 7
         * File2: multiFile/dir1/file2  --  length 78
         * Piece length: 50
         * Num pieces: 2 (50 + 35)
         */
        String info = "d5:filesld6:lengthi7e4:pathl5:file1eed6:lengthi78e4:pathl4:dir15:file2eee" +
                "4:name9:multiFile12:piece lengthi50e6:pieces40:0123456789012345678901234567890123456789e";
        String torrent = "d8:announce35:https://torrent.ubuntu.com/announce13:announce-list" +
                "ll35:https://torrent.ubuntu.com/announceel40:https://ipv6.torrent.ubuntu.com/announceee" +
                "7:comment29:Ubuntu CD releases.ubuntu.com13:creation datei1571322740e4:info" + info + "e";

        return new MetadataInfo(new ByteArrayInputStream(torrent.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void writePieceMultipleFiles() {
        try {
            MetadataInfo meta = createTestMeta();
            Storage storage = new Storage(meta);
            storage.createFiles();

            Piece piece1 = new Piece(0, 50, null);
            Piece piece2 = new Piece(1, 35, null);
            Arrays.fill(piece1.bytes, (byte) 9);
            Arrays.fill(piece2.bytes, (byte) 7);

            /* Write piece1 = 50 bytes */
            storage.writePiece(piece1);

            /* Read 2 blocks of this piece */
            byte[] block1 = storage.readBlock(0, 0, 25);
            byte[] block2 = storage.readBlock(0, 25, 25);
            byte[] block1expected = new byte[25];
            Arrays.fill(block1expected, (byte) 9);
            assertArrayEquals(block1, block1expected);
            assertArrayEquals(block2, block1expected);

            /* Write piece2 = 35 bytes */
            storage.writePiece(piece2);

            /* Read 2 blocks of this piece */
            byte[] block3 = storage.readBlock(1, 0, 25);
            byte[] block4 = storage.readBlock(1, 25, 10);
            byte[] block3expected = new byte[25];
            byte[] block4expected = new byte[10];
            Arrays.fill(block3expected, (byte) 7);
            Arrays.fill(block3expected, (byte) 7);
            assertArrayEquals(block3, block3expected);
            assertArrayEquals(block4, block4expected);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            /* Cleanup */
            File f1 = new File("multiFile/file1");
            File f2 = new File("multiFile/dir1/file2");
            File dir1 = new File("multiFile/dir1");
            File dirParent = new File("multiFile");

            if (f1.exists())
                f1.delete();
            if (f2.exists())
                f2.delete();
            if (dir1.exists())
                dir1.delete();
            if (dirParent.exists())
                dirParent.delete();
        }

    }

    @Test
    public void readBlock() {
    }
}
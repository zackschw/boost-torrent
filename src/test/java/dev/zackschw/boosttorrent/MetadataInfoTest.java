package dev.zackschw.boosttorrent;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class MetadataInfoTest {

    @Test
    public void singleFileTest() throws Exception {
        String info = "d6:lengthi78e4:name34:ubuntu-19.10-live-server-amd64.iso12:piece lengthi40e6:pieces" +
                "40:0123456789012345678901234567890123456789e";
        String torrent = "d8:announce35:https://torrent.ubuntu.com/announce13:announce-list" +
                "ll35:https://torrent.ubuntu.com/announceel40:https://ipv6.torrent.ubuntu.com/announceee" +
                "7:comment29:Ubuntu CD releases.ubuntu.com13:creation datei1571322740e4:info" + info + "e";

        MetadataInfo metadata = new MetadataInfo(new ByteArrayInputStream(torrent.getBytes(StandardCharsets.UTF_8)));

        assertArrayEquals(hash(info.getBytes(StandardCharsets.UTF_8)), metadata.getInfoHash());
        assertEquals("https://torrent.ubuntu.com/announce", metadata.getAnnounce());
        assertEquals(List.of(
                List.of("https://torrent.ubuntu.com/announce"),
                List.of("https://ipv6.torrent.ubuntu.com/announce")),
                metadata.getAnnounceList());
        assertEquals("ubuntu-19.10-live-server-amd64.iso", metadata.getName());
        assertEquals(40, metadata.getPieceLength());
        assertEquals(2, metadata.getNumPieces());
        assertEquals("ubuntu-19.10-live-server-amd64.iso", metadata.getFiles().get(0).getPath());
        assertEquals(78, metadata.getFiles().get(0).getLength());
        assertEquals(1, metadata.getFiles().size());
        assertEquals(38, metadata.getLastPieceLength());
        assertEquals(78, metadata.getTotalFileBytes());
    }

    @Test
    public void multiFileTest() throws Exception {
        String info = "d5:filesld6:lengthi7e4:pathl5:file1eed6:lengthi78e4:pathl4:dir15:file2eee" +
                "4:name9:multiFile12:piece lengthi50e6:pieces40:0123456789012345678901234567890123456789e";
        String torrent = "d8:announce35:https://torrent.ubuntu.com/announce13:announce-list" +
                "ll35:https://torrent.ubuntu.com/announceel40:https://ipv6.torrent.ubuntu.com/announceee" +
                "7:comment29:Ubuntu CD releases.ubuntu.com13:creation datei1571322740e4:info" + info + "e";

        MetadataInfo metadata = new MetadataInfo(new ByteArrayInputStream(torrent.getBytes(StandardCharsets.UTF_8)));

        assertArrayEquals(hash(info.getBytes(StandardCharsets.UTF_8)), metadata.getInfoHash());
        assertEquals("https://torrent.ubuntu.com/announce", metadata.getAnnounce());
        assertEquals(List.of(
                List.of("https://torrent.ubuntu.com/announce"),
                List.of("https://ipv6.torrent.ubuntu.com/announce")),
                metadata.getAnnounceList());
        assertEquals("multiFile", metadata.getName());
        assertEquals(50, metadata.getPieceLength());
        assertEquals(2, metadata.getNumPieces());
        assertEquals("multiFile/file1", metadata.getFiles().get(0).getPath());
        assertEquals(7, metadata.getFiles().get(0).getLength());
        assertEquals("multiFile/dir1/file2", metadata.getFiles().get(1).getPath());
        assertEquals(78, metadata.getFiles().get(1).getLength());
        assertEquals(2, metadata.getFiles().size());
        assertEquals(35, metadata.getLastPieceLength());
        assertEquals(85, metadata.getTotalFileBytes());
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
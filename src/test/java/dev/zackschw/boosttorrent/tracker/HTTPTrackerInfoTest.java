package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.PeerAddress;
import dev.zackschw.boosttorrent.bencode.BValue;
import dev.zackschw.boosttorrent.bencode.BencodeException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class HTTPTrackerInfoTest {

    @Test
    public void testDictPeers() throws Exception {
        BValue peersBV = new BValue(List.of(
                        new BValue(
                                Map.of("peer id", new BValue("excluded".getBytes(StandardCharsets.UTF_8)),
                                        "ip", new BValue("127.0.0.1".getBytes(StandardCharsets.UTF_8)),
                                        "port", new BValue("65000".getBytes(StandardCharsets.UTF_8)))),
                        new BValue(
                                Map.of("peer id", new BValue("excluded".getBytes(StandardCharsets.UTF_8)),
                                        "ip", new BValue("255.255.255.0".getBytes(StandardCharsets.UTF_8)),
                                        "port", new BValue("54321".getBytes(StandardCharsets.UTF_8)))),
                        new BValue(
                                Map.of("peer id", new BValue("excluded".getBytes(StandardCharsets.UTF_8)),
                                        "ip", new BValue("129.1.100.100".getBytes(StandardCharsets.UTF_8)),
                                        "port", new BValue("2222".getBytes(StandardCharsets.UTF_8))))));


        HTTPTrackerInfo fakeTracker = new HTTPTrackerInfo("http://www.google.com", null, null);
        fakeTracker.decodePeers(peersBV);
        List<PeerAddress> peers = fakeTracker.getPeers();

        assertEquals("127.0.0.1", peers.get(0).getAddress().getHostAddress());
        assertEquals(65000, peers.get(0).getPort());
        assertEquals("255.255.255.0", peers.get(1).getAddress().getHostAddress());
        assertEquals(54321, peers.get(1).getPort());
        assertEquals("129.1.100.100", peers.get(2).getAddress().getHostAddress());
        assertEquals(2222, peers.get(2).getPort());
    }

    @Test
    public void testBinaryPeers() throws IOException, BencodeException {
        BValue peersBV = new BValue(new byte[] {
                127,0,0,1, (byte)0xfd, (byte)0xe8,
                (byte)255,(byte)255,(byte)255,0, (byte)0xd4, 0x31,
                (byte)129,1,100,100, 0x08, (byte)0xae });


        HTTPTrackerInfo fakeTracker = new HTTPTrackerInfo("http://www.google.com", null, null);
        fakeTracker.decodePeers(peersBV);
        List<PeerAddress> peers = fakeTracker.getPeers();

        assertEquals("127.0.0.1", peers.get(0).getAddress().getHostAddress());
        assertEquals(65000, peers.get(0).getPort());
        assertEquals("255.255.255.0", peers.get(1).getAddress().getHostAddress());
        assertEquals(54321, peers.get(1).getPort());
        assertEquals("129.1.100.100", peers.get(2).getAddress().getHostAddress());
        assertEquals(2222, peers.get(2).getPort());
    }

}
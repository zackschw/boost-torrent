package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.PeerAddress;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UDPTrackerInfoTest {
    @Test
    void testUDP() throws Exception {
        byte[] received = new byte[] {
                0,0,0,1, // action: announce
                0x64,0x4d,0x2b,(byte)0xe6, // transaction id
                1,2,3,4, // interval
                1,2,3,4, // leechers
                1,2,3,4, // seeders
                127,0,0,1, (byte)0xfd, (byte)0xe8, // peer 1
                (byte)255,(byte)255,(byte)255,0, (byte)0xd4, 0x31, // peer 2
                (byte)129,1,100,100, 0x08, (byte)0xae // peer 3
        };

        UDPTrackerInfo tracker = new UDPTrackerInfo("udp://google.com:33", null, 2222, null);
        tracker.decodePeers(received, received.length, 0x644d2be6);
        List<PeerAddress> peers = tracker.getPeers();

        assertEquals("127.0.0.1", peers.get(0).getAddress().getHostAddress());
        assertEquals(65000, peers.get(0).getPort());
        assertEquals("255.255.255.0", peers.get(1).getAddress().getHostAddress());
        assertEquals(54321, peers.get(1).getPort());
        assertEquals("129.1.100.100", peers.get(2).getAddress().getHostAddress());
        assertEquals(2222, peers.get(2).getPort());
    }
}
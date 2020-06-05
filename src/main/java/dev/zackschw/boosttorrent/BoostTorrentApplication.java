package dev.zackschw.boosttorrent;

import dev.zackschw.boosttorrent.bencode.BencodeException;
import dev.zackschw.boosttorrent.tracker.TrackerCoordinator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;


public class BoostTorrentApplication {

    public static void main(String[] args) {
        if (args.length != 2) {
            usage();
        }

        String filename = args[1];

        MetadataInfo meta;
        try {
            meta = new MetadataInfo(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found");
        } catch (IOException | BencodeException e) {
            throw new RuntimeException("Error processing metadata: " + e);
        }

        byte[] peerID = createPeerID();
        Storage storage = new Storage(meta);
        PeerCoordinator coordinator = new PeerCoordinator(peerID, meta, storage);

        /* Run */
        try {
            coordinator.initiateConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void usage() {
        System.out.println("Usage: ./boost [filename|magnet_link");
        System.exit(1);
    }

    private static byte[] createPeerID() {
        byte[] myPeerID = new byte[20];
        Random random = new Random();
        myPeerID[0] = '-';
        myPeerID[1] = 'X';
        myPeerID[2] = 'D';
        myPeerID[3] = '1';
        myPeerID[4] = myPeerID[5] = myPeerID[6] = '0';
        myPeerID[7] = '-';
        for (int i=8; i < 20; i++)
            myPeerID[i] = (byte) (random.nextInt(256));

        return myPeerID;
    }
}

package dev.zackschw.boosttorrent.tracker;

import dev.zackschw.boosttorrent.PeerAddress;
import dev.zackschw.boosttorrent.PeerCoordinator;
import dev.zackschw.boosttorrent.bencode.BDecoder;
import dev.zackschw.boosttorrent.bencode.BValue;
import dev.zackschw.boosttorrent.bencode.BencodeException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPTrackerInfo implements TrackerInfo {
    private final String url;
    private final byte[] infoHash;
    private final int port;

    private final PeerCoordinator coordinator;

    private String trackerID;
    private final List<PeerAddress> peers;


    public HTTPTrackerInfo(String url, byte[] infoHash, int port, PeerCoordinator coordinator) {
        this.url = url;
        this.infoHash = infoHash;
        this.port = port;

        this.coordinator = coordinator;

        peers = new ArrayList<>();
    }

    @Override
    public boolean sendStarted() {
        try {
            return doRequest(EVENT_STARTED);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public boolean sendCompleted() {
        try {
            return doRequest(EVENT_COMPLETED);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public boolean sendStopped() {
        try {
            return doRequest(EVENT_STOPPED);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public boolean sendEmpty() {
        try {
            return doRequest(null);
        } catch (IOException e) {
            // XXX log
            return false;
        }
    }

    @Override
    public List<PeerAddress> getPeers() {
        return peers;
    }


    /**
     * Sends request to the http url
     * @param event event to send,
     *              which must be one of "started", "stopped", "completed", or null for no event/unspecified
     * @return true on successful connection and response, otherwise false
     * @throws IOException on connection I/O error
     */
    private boolean doRequest(String event) throws IOException {
        String infoHashStr = urlEncode(infoHash);
        String myPeerIDStr = urlEncode(coordinator.getMyPeerID());
        long downloaded = coordinator.getDownloaded();
        long uploaded = coordinator.getUploaded();
        long left = coordinator.getLeft();

        String request = String.format("%s?%s=%s&%s=%s&%s=%d&%s=%d&%s=%d&%s=%d&%s=%d%s%s",
                url,
                "info hash", infoHashStr,
                "peer_id", myPeerIDStr,
                "port", port,
                "uploaded", uploaded,
                "downloaded", downloaded,
                "left", left,
                "compact", 1,
                event == null ? "" : String.format("&%s=%s", "event", event),
                trackerID == null ? "" : String.format("&%s=%s", "trackerid", trackerID));

        // open connection
        URL url = new URL(request);
        HttpURLConnection connection = openConnectionCheckRedirects(url);

        // connect
        connection.connect();

        // read response
        if (!checkResponseCode(connection)) {
            return false;
        }

        if (event == EVENT_STARTED || event == null) {
            try {
                readResponse(connection);
            } catch (BencodeException e) {
                return false;
            }
        }


        return true;
    }

    /**
     * Opens a connection to the url and follows any redirects given by HTTP 3XX response codes, up to 5 redirects.
     * @return The final opened HttpURLConnection
     * @throws IOException on connection I/O error
     */
    private static HttpURLConnection openConnectionCheckRedirects(URL url) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        boolean redir;
        int redirects = 0;
        do {
            httpConn.setInstanceFollowRedirects(false);
            httpConn.connect();
            redir = false;
            int stat = httpConn.getResponseCode();
            if (stat >= 300 && stat <= 307 && stat != 306 && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                URL base = httpConn.getURL();
                String loc = httpConn.getHeaderField("Location");
                URL target = null;
                if (loc != null) {
                    target = new URL(base, loc);
                }
                httpConn.disconnect();

                // Redirection should be allowed only for HTTP and HTTPS
                // and should be limited to 5 redirections at most.
                if (target == null || !(target.getProtocol().equals("http") || target.getProtocol().equals("https"))
                        || redirects >= 5) {
                    throw new IOException("Illegal URL redirect");
                }
                redir = true;
                httpConn = (HttpURLConnection) target.openConnection();
                redirects++;
            }
        } while (redir);

        return httpConn;
    }

    /**
     * Checks for a successful response code (2XX) given by the connection with the tracker.
     * @param connection connection that has already been opened and connected to.
     * @return true if connection was successful, otherwise false
     * @throws IOException if an error occurred connecting to the server.
     */
    private boolean checkResponseCode(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        return status >= 200 && status < 300;
    }

    /**
     * Reads the tracker response from the open connection.
     * The list of peers received is added to the HTTPTrackerInfo's list of peers.
     * @param connection connection that has already been opened and connected to.
     * @throws IOException on read error
     */
    private void readResponse(HttpURLConnection connection) throws IOException, BencodeException {
        BDecoder decoder = new BDecoder(connection.getInputStream());
        Map<String, BValue> trackerResponse = decoder.readDict().getMap();
        decoder.close();

        if (trackerResponse.containsKey("failure reason"))
            throw new RuntimeException(trackerResponse.get("failure reason").getString());

        if (trackerResponse.containsKey("tracker id"))
            trackerID = trackerResponse.get("tracker id").getString();

        decodePeers(trackerResponse.get("peers"));
    }

    void decodePeers(BValue peersBV) throws BencodeException {
        if (peersBV == null)
            return;

        if (peersBV.getValue() instanceof  byte[]) {
            /* Binary model */
            byte[] peers = peersBV.getBytes();

            // TODO decode and add each peer to List<PeerAddress> peers
        }

        else if (peersBV.getValue() instanceof ArrayList) {
            /* List of dictionaries model */
            List<BValue> peersList = peersBV.getList();

            // TODO decode and add each peer to List<PeerAddress> peers
        }
    }


    /**
     * URL encodes the byte array input by encoding any byte not equal to a-z, A-Z, "." "-", or "_" as %XX,
     * where XX is the hex representation of the byte.
     * @return The url-encoded String
     */
    private static String urlEncode(byte[] input) {
        StringBuilder s = new StringBuilder();
        for (byte b : input) {
            if ((b >= 'a' && b <= 'z')
                    || (b >= 'A' && b <= 'Z')
                    || (b >= '0' && b <= '9')
                    || b == '-' || b == '_' || b == '.')
                s.append((char) b);
            else
                s.append(String.format("%%%02x", b));
        }

        return s.toString();
    }

    private static final String EVENT_STARTED = "started";
    private static final String EVENT_COMPLETED = "completed";
    private static final String EVENT_STOPPED = "stopped";
}

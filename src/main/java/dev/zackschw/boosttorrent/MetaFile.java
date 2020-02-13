package dev.zackschw.boosttorrent;

public class MetaFile {
    private final String path;
    private final long length;

    public MetaFile(String path, long length) {
        this.path = path;
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public long getLength() {
        return length;
    }

}

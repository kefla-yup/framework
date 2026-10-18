package framework;

public class MapFile {
    private String fileName;
    private String extension;
    private byte[] bytes;

    public MapFile() {}

    public MapFile(String fileName, String extension, byte[] bytes) {
        this.fileName = fileName;
        this.extension = extension;
        this.bytes = bytes;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) { this.bytes = bytes; }
}
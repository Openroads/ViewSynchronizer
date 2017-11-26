package pk.edu.dariusz.viewsynchronizer.server;

import java.io.InputStream;

/**
 * Created by dariusz on 11/25/17.
 */

public class DataObjectToSend {
    private DATA_TYPE type;
    private String message;
    private InputStream fileInputStream;
    private long length;

    public DataObjectToSend(String message) {
        this.message=message;
        this.type = DATA_TYPE.STRING_MSG;
    }

    public DATA_TYPE getType() {
        return type;
    }

    public void setType(DATA_TYPE type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public void setFileInputStream(InputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}

package pk.edu.dariusz.viewsynchronizer.server;

import android.net.Uri;

import java.io.File;
import java.io.InputStream;

import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;

/**
 * Created by dariusz on 11/25/17.
 */

public class DataObjectToSend {
    private DATA_TYPE type;
    private String message;
    private Uri fileUri;
    private File file;
    private String fileName;
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

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

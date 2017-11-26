package pk.edu.dariusz.viewsynchronizer.client;

import java.io.File;

import pk.edu.dariusz.viewsynchronizer.server.DATA_TYPE;

/**
 * Created by dariusz on 11/25/17.
 */

public class LeaderDataObject {
    private DATA_TYPE type;

    private String message;
    private File file;

    public LeaderDataObject(String message) {
    this.message=message;
    }

    public LeaderDataObject() {

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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}

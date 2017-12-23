package pk.edu.dariusz.viewsynchronizer.server.model;

import java.io.File;

import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;
import pk.edu.dariusz.viewsynchronizer.server.model.UriInfo;

/**
 * Created by dariusz on 11/25/17.
 */

public class DataObjectToSend {
    private DATA_TYPE type;
    private String message;
    private File file;
    private boolean isFileAllowedToDownload;
    private UriInfo uriInfo;


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


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isFileAllowedToDownload() {
        return isFileAllowedToDownload;
    }

    public void setFileAllowedToDownload(boolean fileAllowedToDownload) {
        isFileAllowedToDownload = fileAllowedToDownload;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
}

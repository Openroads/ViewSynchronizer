package pk.edu.dariusz.viewsynchronizer.client.model;

import java.io.File;

import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

/**
 * Created by dariusz on 11/25/17.
 */

public class LeaderDataObject {
    private DATA_TYPE type;

    private String message;
    private File file;
    private long fileSizeCheckSum;

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

    public long getFileSizeCheckSum() {
        return fileSizeCheckSum;
    }

    public void setFileSizeCheckSum(long fileSizeCheckSum) {
        this.fileSizeCheckSum = fileSizeCheckSum;
    }
    public boolean isDataFileCorrect(){
        if(file ==null)
            return true;
        else {
            LogUtil.logDebugToConsole("F.length: " +file.length()+ "checksumfromserver:" +fileSizeCheckSum);
            return file.length() == this.fileSizeCheckSum;
        }
        }
}

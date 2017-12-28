package pk.edu.dariusz.viewsynchronizer.client.model;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

/**
 * Created by dariusz on 11/25/17.
 */

public class LeaderDataObject {
    private DATA_TYPE type;

    private String message;
    private File file;
    private String originalFileName;
    private boolean isAllowedToDownload;
    private long fileSizeCheckSum;
    private int downloadProgress;
    private final AtomicInteger downloadProgressAtomic = new AtomicInteger(0);

    public LeaderDataObject(String message) {
    this.message=message;
    type=DATA_TYPE.STRING_MSG;
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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public boolean isAllowedToDownload() {
        return isAllowedToDownload;
    }

    public void setAllowedToDownload(boolean allowedToDownload) {
        isAllowedToDownload = allowedToDownload;
    }

    public long getFileSizeCheckSum() {
        return fileSizeCheckSum;
    }

    public void setFileSizeCheckSum(long fileSizeCheckSum) {
        this.fileSizeCheckSum = fileSizeCheckSum;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public AtomicInteger getDownloadProgressAtomic() {
        return downloadProgressAtomic;
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

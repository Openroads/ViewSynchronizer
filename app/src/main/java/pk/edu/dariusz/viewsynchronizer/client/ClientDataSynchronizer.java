package pk.edu.dariusz.viewsynchronizer.client;

import android.os.Environment;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import pk.edu.dariusz.viewsynchronizer.client.model.LeaderDataObject;
import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;
import pk.edu.dariusz.viewsynchronizer.commons.REQUEST_TYPE;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;

/**
 * Created by dariusz on 11/15/17.
 */

public class ClientDataSynchronizer {
    private Socket serverSocket;
    private SocketAddress socketAddress;
    private BufferedReader in;
    private File directoryForData;

    public ClientDataSynchronizer(String address, int port,File directoryForData) throws IOException {
        socketAddress = new InetSocketAddress(address,port);
        this.directoryForData=directoryForData;
/*        in = new BufferedReader(new InputStreamReader(
                serverSocket.getInputStream()));*/
    }
    public void closeSynchronizer() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }


    public LeaderDataObject fetchDataFromServer(REQUEST_TYPE request_type,LeaderDataObject leaderDataObject) throws IOException {

        //making reconnection to server
        reconnect();

        InputStream inputStream = serverSocket.getInputStream();
        //PrintWriter writer = new PrintWriter(serverSocket.getOutputStream());
        //InputStreamReader reader = new InputStreamReader(inputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(serverSocket.getOutputStream());
        dataOutputStream.writeUTF(request_type.name());
        dataOutputStream.flush();

        DataInputStream dataInputStream = new DataInputStream(inputStream);


        String dataType = dataInputStream.readUTF();//.split(";");
        long size = dataInputStream.readLong();
        String message = dataInputStream.readUTF();
        DATA_TYPE type = DATA_TYPE.valueOf(dataType);
        leaderDataObject.setType(type);
        leaderDataObject.setMessage(message);
        leaderDataObject.setFileSize(size);
        switch (type) {
            case STRING_MSG:
                break;
            case OTHER:
            case PDF:
            case IMG:
                String fileName = dataInputStream.readUTF();
                LogUtil.logDebugToConsole("FILE NAME: " +fileName);
                leaderDataObject.setOriginalFileName(fileName);
                boolean allowedToDownload = dataInputStream.readBoolean();
                LogUtil.logDebugToConsole("DOWNLOADING: " +allowedToDownload);
                leaderDataObject.setAllowedToDownload(allowedToDownload);

                // WRITING TO EXTERNAL STORAGE
                /*String extension = FilenameUtils.getExtension(fileName);
                File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                        +File.separator + "temp."+extension);*/
                try (FileOutputStream out = new FileOutputStream(directoryForData)) {
                    byte[] buf = new byte[8192];
                    int len = 0;
                    int total=0;
                    while ((len = inputStream.read(buf)) != -1) {
                        total+=len;
                        out.write(buf, 0, len);
                        leaderDataObject.getDownloadProgressAtomic().set((int)(((double)total/(int)size) *100));
                    }
                    inputStream.close();
                    serverSocket.close();
                    serverSocket=null;
                    leaderDataObject.setFile(directoryForData);
                }
                    break;
            }
        return leaderDataObject;
    }


    public File fetchFileFromServer() throws IOException {
        InputStream in = serverSocket.getInputStream();
        File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/rec.jpg");
        LogUtil.logDebugToConsole("EXTSTORAGE_WR: "+isExternalStorageWritable());
        LogUtil.logDebugToConsole("File path to write: " + outFile.getAbsolutePath());
        OutputStream out = new FileOutputStream(outFile);
        Utils.copyBetweenStreams(in,out);
        LogUtil.logDebugToConsole("Finished reading image ");
        in.close();
        out.close();
        return outFile;
    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void reconnect() throws IOException {
        this.serverSocket = new Socket();
        this.serverSocket.connect(socketAddress);
    }
}

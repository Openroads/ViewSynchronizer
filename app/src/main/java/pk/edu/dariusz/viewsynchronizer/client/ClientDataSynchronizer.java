package pk.edu.dariusz.viewsynchronizer.client;

import android.os.Environment;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

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

    public ClientDataSynchronizer(String address, int port) throws IOException {
        socketAddress = new InetSocketAddress(address,port);
/*        in = new BufferedReader(new InputStreamReader(
                serverSocket.getInputStream()));*/
    }
    public void closeSynchronizer() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }


    public LeaderDataObject fetchDataFromServer(REQUEST_TYPE request_type) throws IOException {

        //making reconnection to server
        reconnect();

        InputStream inputStream = serverSocket.getInputStream();
        //PrintWriter writer = new PrintWriter(serverSocket.getOutputStream());
        //InputStreamReader reader = new InputStreamReader(inputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(serverSocket.getOutputStream());
        dataOutputStream.writeUTF(request_type.name());
        dataOutputStream.flush();

        DataInputStream dataInputStream = new DataInputStream(inputStream);

        LeaderDataObject leaderDataObject = new LeaderDataObject();
        String headers = dataInputStream.readUTF();//.split(";");
        long size = dataInputStream.readLong();
        String message = dataInputStream.readUTF();
        /*String datatype = "DataType=";
        int lastIndexOf = headers[0].indexOf(datatype) + datatype.length();
        datatype = headers[0].substring(lastIndexOf, lastIndexOf + 1);
        String length="Length=";
        lastIndexOf = headers[0].indexOf(length) + datatype.length();
        length = headers[0].substring(lastIndexOf, lastIndexOf + 1);*/
        //DATA_TYPE type = DATA_TYPE.values()[Integer.parseInt(headers[0])];
        DATA_TYPE type = DATA_TYPE.valueOf(headers);
        leaderDataObject.setType(type);
        leaderDataObject.setMessage(message);
        leaderDataObject.setFileSizeCheckSum(size);
        switch (type) {
            case STRING_MSG:
                break;
            case OTHER:
            case PDF:
            case IMG:
                String fileName = dataInputStream.readUTF();
                LogUtil.logDebugToConsole("FILE NAME: " +fileName);
                String extension = FilenameUtils.getExtension(fileName);
                File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+File.separator + "temp."+extension);
                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    //int bytes = IOUtils.copy(inputStream,out);
                    /*long lenght = Long.parseLong(headers[1]);
                    //Utils.copyBetweenStreams(inputStream, out);
                    long bytesToRead=lenght;
                    byte[] buf = new byte[8192];
                    int len = 0;
                    while (bytesToRead >0 && (len = inputStream.read(buf,0, (int)Math.min(buf.length, bytesToRead))) != -1)  {
                        out.write(buf, 0, len);
                        bytesToRead-=len;
                        LogUtil.logInfoToConsole(String.valueOf(bytesToRead));
                    }
                    LogUtil.logInfoToConsole(String.valueOf(bytesToRead));*/

                    IOUtils.copy(inputStream,out);
                    inputStream.close();
                    serverSocket.close();
                    serverSocket=null;
                    leaderDataObject.setFile(outFile);
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

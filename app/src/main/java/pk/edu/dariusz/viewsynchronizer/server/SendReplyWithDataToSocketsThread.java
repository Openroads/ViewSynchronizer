package pk.edu.dariusz.viewsynchronizer.server;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

/**
 * Created by dariusz on 11/19/17.
 */

public class SendReplyWithDataToSocketsThread extends Thread {

    private List<Socket> hostSockets;
    private DataObjectToSend data;


    public SendReplyWithDataToSocketsThread(List<Socket> sockets,DataObjectToSend m) {
        this.hostSockets = sockets;
        this.data = m;
    }


    public SendReplyWithDataToSocketsThread(Socket socket, DataObjectToSend m) {
        this.hostSockets =new LinkedList<>(Arrays.asList(socket));
        this.data = m;
    }


    @Override
    public void run() {
        List<Socket> disconnectedClients =null;
        LogUtil.logDebugToConsole("Response is to : " + hostSockets.size() + " listeners");
        for (Socket socket : hostSockets) {
            try {
                sendMessageToSocket(socket, data);
                socket.close();
               /* if (!Utils.checkIfClientIsConnected(socket)) {
                    if(disconnectedClients == null) disconnectedClients = new ArrayList<>(1);
                    socket.close();
                    disconnectedClients.add(socket);
                }*/
            } catch (Exception e) {
                LogUtil.logErrorToConsole("Exception during sending  message to all connected hosts.", e);
            }
        }
        hostSockets.clear();
  //     if(disconnectedClients != null) hostSockets.removeAll(disconnectedClients);
    }

    private void sendMessageToSocket(Socket socket, DataObjectToSend data) throws IOException {
        //String header = "DataType=" + data.getType().ordinal()+";Length="+data.getFileInputStream().available();
        String dataType = data.getType().name();
        //if(data.getFileInputStream()!=null)header+=";"+data.getLength();
        OutputStream socketOutputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(socketOutputStream);

        dataOutputStream.writeUTF(dataType);
        long length = data.getFile() !=null ?  data.getUriInfo().getLength() : 0;
        dataOutputStream.writeLong(length);
        dataOutputStream.writeUTF(data.getMessage());
       // dataOutputStream.flush();

        switch (data.getType()) {
            case STRING_MSG:
                break;
            case IMG:
            case PDF:
            case OTHER:
                dataOutputStream.writeUTF(data.getUriInfo().getFullFileName());
                LogUtil.logInfoToConsole("Sending binary filee to listener. Size: " + data.getFile().length());
               try( BufferedOutputStream bos = new BufferedOutputStream(socketOutputStream);
                    FileInputStream fis = new FileInputStream(data.getFile())) {
                   IOUtils.copy(fis, bos);
//                   socketOutputStream.flush();
//                   bos.close();
               }
                LogUtil.logInfoToConsole("Flushed binary data");
                break;
        }


    }
}

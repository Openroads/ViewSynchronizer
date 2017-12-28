package pk.edu.dariusz.viewsynchronizer.server;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import pk.edu.dariusz.viewsynchronizer.server.model.DataObjectToSend;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

/**
 * Created by dariusz on 11/19/17.
 */

public class SendReplyWithDataToSocketsThread extends Thread {

    private List<Socket> hostSockets;
    private DataObjectToSend data;


    public SendReplyWithDataToSocketsThread(List<Socket> sockets, DataObjectToSend m) {
        this.hostSockets = sockets;
        this.data = m;

    }


    public SendReplyWithDataToSocketsThread(Socket socket, DataObjectToSend m) {
        this.hostSockets =new LinkedList<>(Arrays.asList(socket));
        this.data = m;
    }


    @Override
    public void run() {
        LogUtil.logDebugToConsole("Response is to : " + hostSockets.size() + " listeners");
        for (Socket socket : hostSockets) {
            try {
                sendMessageToSocket(socket, data);
                socket.close();
            } catch (Exception e) {
                LogUtil.logErrorToConsole("Exception during sending  message to all connected hosts.", e);
            }
        }
        hostSockets.clear();
    }

    private void sendMessageToSocket(Socket socket, DataObjectToSend data) throws IOException {

        String dataType = data.getType().name();
        OutputStream socketOutputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(socketOutputStream);

        dataOutputStream.writeUTF(dataType);
        long length = data.getFile() !=null ?  data.getUriInfo().getLength() : 0;
        dataOutputStream.writeLong(length);
        dataOutputStream.writeUTF(data.getMessage());

        switch (data.getType()) {
            case STRING_MSG:
                break;
            case IMG:
            case PDF:
            case OTHER:
                dataOutputStream.writeUTF(data.getUriInfo().getFullFileName());
                dataOutputStream.writeBoolean(data.isFileAllowedToDownload());
                LogUtil.logInfoToConsole("Sending binary file to listener. Size: " + data.getFile().length());
               try( BufferedOutputStream bos = new BufferedOutputStream(socketOutputStream);
                    FileInputStream fis = new FileInputStream(data.getFile())) {
                   IOUtils.copy(fis, bos);
               }
                LogUtil.logInfoToConsole("Flushed binary data");
                break;
        }
    }
}

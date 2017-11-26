package pk.edu.dariusz.viewsynchronizer.server;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
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
        this.hostSockets =new LinkedList<>(Collections.singletonList(socket));
        this.data = m;
    }


    @Override
    public void run() {
        List<Socket> disconnectedClients =null;
        for (Socket socket : hostSockets) {
            try {
                sendMessageToSocket(socket, data);

               /* if (!Utils.checkIfClientIsConnected(socket)) {
                    if(disconnectedClients == null) disconnectedClients = new ArrayList<>(1);
                    socket.close();
                    disconnectedClients.add(socket);
                }*/
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Exception during sending  message to all connected hosts.", e);
                e.printStackTrace();
            }
        }
        hostSockets.clear();
  //     if(disconnectedClients != null) hostSockets.removeAll(disconnectedClients);
    }

    private void sendMessageToSocket(Socket socket, DataObjectToSend data) throws IOException {
        String msgReply = "Hello from Server, we have " + hostSockets.size() + " listeners."
                + "Message from leader is: " + data.getMessage();
        LogUtil.logDebugToConsole("Response is to : " + hostSockets.size() + " listeners");

        //String header = "DataType=" + data.getType().ordinal()+";Length="+data.getFileInputStream().available();
        String header = String.valueOf(data.getType().ordinal()).trim();
        header=data.getType().name();
        if(data.getFileInputStream()!=null)header+=";"+data.getLength();
        OutputStream socketOutputStream = socket.getOutputStream();
        BufferedWriter writerToSocket = new BufferedWriter(new OutputStreamWriter(socketOutputStream));


        writerToSocket.write(header);
        writerToSocket.newLine();
        writerToSocket.write(data.getMessage());
        writerToSocket.newLine();
        writerToSocket.flush();

        switch (data.getType()) {
            case STRING_MSG:
                break;
            case IMG:
            case PDF:
            case OTHER:
                LogUtil.logInfoToConsole("Sending binary fie to listener. header: " + header);
                BufferedOutputStream bos = new BufferedOutputStream(socketOutputStream);
                IOUtils.copy(data.getFileInputStream(), bos);
                socketOutputStream.flush();
                data.getFileInputStream().close();
                socketOutputStream.close();
                socketOutputStream.close();
                LogUtil.logInfoToConsole("Flushed binary data");
        }


    }
}

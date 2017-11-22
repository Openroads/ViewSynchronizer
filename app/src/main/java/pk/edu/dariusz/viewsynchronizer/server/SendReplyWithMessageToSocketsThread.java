package pk.edu.dariusz.viewsynchronizer.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;

/**
 * Created by dariusz on 11/19/17.
 */

public class SendReplyWithMessageToSocketsThread extends Thread {

    private List<Socket> hostSockets;
    private String message;


    SendReplyWithMessageToSocketsThread(List<Socket> sockets,String m) {
        this.hostSockets = sockets;
        this.message = m;
    }


    SendReplyWithMessageToSocketsThread(Socket socket, String m) {
        this.hostSockets = Collections.singletonList(socket);
        this.message = m;
    }


    @Override
    public void run() {
        List<Socket> disconnectedClients =null;
        for (Socket socket : hostSockets) {
            try {
                sendMessageToSocket(socket, message);

                if (!Utils.checkIfClientIsConnected(socket)) {
                    if(disconnectedClients == null) disconnectedClients = new ArrayList<>(1);
                    socket.close();
                    disconnectedClients.add(socket);
                }
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Exception during sending  message to all connected hosts.", e);
                e.printStackTrace();
            }
        }
        if(disconnectedClients != null) hostSockets.removeAll(disconnectedClients);
    }

    private void sendMessageToSocket(Socket socket, String message) throws IOException {
        String msgReply = "Hello from Server, we have " + hostSockets.size() + " listeners."
                + "Message from leader is: " + message;
        OutputStream outputStream = null;
        LogUtil.logDebugToConsole("Response is to : "+hostSockets.size() + " listeners");
        try {
            outputStream = socket.getOutputStream();
           /* PrintStream printStream = new PrintStream(outputStream);
            printStream.print(msgReply);
            printStream.close();*/
            PrintWriter out = new PrintWriter(outputStream,true);
            out.println(msgReply);

        } finally {
           /* if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }*/
        }
    }
}

package pk.edu.dariusz.viewsynchronizer.server;

import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pk.edu.dariusz.viewsynchronizer.commons.REQUEST_TYPE;
import pk.edu.dariusz.viewsynchronizer.server.model.DataObjectToSend;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

/**
 * Created by dariusz on 11/15/17.
 */

public class ServerViewSynchronizerImpl implements ServerViewSynchronizer {
    private ServerSocket serverSocketListener;
    private DataObjectToSend dataToSend = new DataObjectToSend("Default data from leader :)");
    private int serverPort;
    private List<Socket> listenersSocket;



    public ServerViewSynchronizerImpl(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getPort() {
        return serverPort;
    }

    public ServerSocket runServer() {
        if(serverSocketListener == null){
            Thread socketServerThread = new ServerThreadListener();
            socketServerThread.start();
        }
        return serverSocketListener;
    }

    public void updateMessageForListeners(DataObjectToSend message) {
        LogUtil.logInfoToConsole("Updating message from: "+this.dataToSend.getMessage()+" to: " +message.getMessage());
        this.dataToSend = message;
        if(listenersSocket != null && listenersSocket.size() > 0) {
            new SendReplyWithDataToSocketsThread(listenersSocket,message).start();
        }else{
            LogUtil.logInfoToConsole("No listeners connected to server.");
        }
    }

    public void closeServer() {
        LogUtil.logDebugToConsole("Closing server - closeServer()");
        if (serverSocketListener != null) {
            try {
                for(Socket socket : listenersSocket)
                    socket.close();
                serverSocketListener.close();

            } catch (IOException e) {
                Log.getStackTraceString(e);
            }
        }
    }
    private class ServerThreadListener extends Thread {

        @Override
        public void run() {
            try {
                serverSocketListener = new ServerSocket(serverPort);
                listenersSocket = new LinkedList<>();
                while (true) {
                    LogUtil.logInfoToConsole("Server is listening now");
                    Socket socket = serverSocketListener.accept();
                    LogUtil.logDebugToConsole("Server has just connected  with " + Arrays.toString(socket.getInetAddress().getAddress()));

                    REQUEST_TYPE operation = null;
                    try{
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        operation = REQUEST_TYPE.valueOf(dataInputStream.readUTF());
                        switch (operation){
                            case FIRST:
                                sendResponseToSocket(socket);
                                break;
                            case GET_NEXT:
                                listenersSocket.add(socket);
                                break;
                            case REFRESH:
                                sendResponseToSocket(socket);
                                break;
                            case FINISH:
                                break;
                        }

                    }catch(IOException clientStreamException){
                            LogUtil.logErrorToConsole("Exception in listener loop server.",clientStreamException);
                    }

                }
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Exception in server accepting connection.",e);
                e.printStackTrace();
            }
        }
    }
    private void sendResponseToSocket(Socket socket){
        new SendReplyWithDataToSocketsThread(socket,dataToSend).start();
    }
}


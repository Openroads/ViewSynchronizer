package pk.edu.dariusz.viewsynchronizer.server;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;

/**
 * Created by dariusz on 11/15/17.
 */

public class ServerImpl implements ServerViewSynchronizer {
    private ServerSocket serverSocketListener;
    private DataObjectToSend dataToSend = new DataObjectToSend("Default data from leader :)");
    private int serverPort;
    private List<Socket> listenersSocket;
    //identifier set to recognise first connecting hosts
    private Set<String> knownListeners=new HashSet<>();


    public ServerImpl(int serverPort) {
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
            new SendReplyWithDataToSocketsThread(listenersSocket,message).run();
        }else{
            LogUtil.logInfoToConsole("No listeners connected to server.");
        }
    }

   /* @Override
    public void sendFileForListeners(InputStream file, DATA_TYPE type) {
        LogUtil.logInfoToConsole("Sending file to listeners. Path: ");
        if(listenersSocket != null && listenersSocket.size() > 0) {
            new SendReplyWithFileToSocketsThread(listenersSocket,file,type).run();
        }else{
            LogUtil.logInfoToConsole("No listeners connected to server.");
        }
    }*/

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
                    // block until new connection is created
                    LogUtil.logDebugToConsole("ServerIsListening now on");
                    Socket socket = serverSocketListener.accept();
                    String clientAddress =Arrays.toString(socket.getInetAddress().getAddress());
                    LogUtil.logInfoToConsole("Servect has just connected  with " + Arrays.toString(socket.getInetAddress().getAddress()));

                    REQUEST_TYPE operation = null;
                    try{
                       // BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        operation = REQUEST_TYPE.valueOf(dataInputStream.readUTF());
                        LogUtil.logInfoToConsole("Request type: " +operation);
                        switch (operation){
                            case FIRST:
                                sendResponseToSocket(socket);
                                knownListeners.add(clientAddress);
                                break;
                            case GET_NEXT:
                  /*              if(!knownListeners.contains(clientAddress) ) {
                                    sendResponseToSocket(socket);
                                    knownListeners.add(clientAddress);
                                }*/
                                listenersSocket.add(socket);
                                break;
                            case REFRESH:
                                sendResponseToSocket(socket);
                                break;
                            case FINISH:
                                knownListeners.remove(clientAddress);
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
//        listenersSocket.remove(socket);
    }
    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }
}


package pk.edu.dariusz.viewsynchronizer.server;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
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
        LogUtil.logInfoToConsole("Updating message from: "+this.dataToSend.getMessage()+" to: " +message);
        if(dataToSend.getFileInputStream()!=null)IOUtils.closeQuietly(this.dataToSend.getFileInputStream());//TODO BE CAREFUL
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
                    listenersSocket.add(socket);
                    if(!knownListeners.contains(clientAddress)){
                        new SendReplyWithDataToSocketsThread(socket,dataToSend).start();
                        knownListeners.add(clientAddress);
                    }

                }
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Exception in server accepting connection.",e);
                e.printStackTrace();
            }
        }
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

/*
    private class SendMessageToAllListenersThread extends Thread {

        private List<Socket> hostSockets;


        SendMessageToAllListenersThread(List<Socket> sockets) {
            this.hostSockets = sockets;
        }

        @Override
        public void run() {
            for (Socket socket : hostSockets) {
                try {
                    sendMessageToSocket(socket, message);

                    if(!Utils.checkIfClientIsConnected(socket)){
                        socket.close();
                        hostSockets.remove(socket);
                    }
                } catch (IOException e) {
                    LogUtil.logErrorToConsole("Exception during sending  message to all connected hosts.",e);
                    e.printStackTrace();
                }
            }
        }
    }



    private class SendReplyWithCurrentMessageThread extends Thread {

        private Socket hostThreadSocket;

        SendReplyWithCurrentMessageThread(Socket socket) {
            hostThreadSocket = socket;
        }

        @Override
        public void run() {
            try {
                sendMessageToSocket(hostThreadSocket, message);
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Exception during sending message to new connected client.",e);
                e.printStackTrace();
            }
        }


    }
         private void sendMessageToSocket(Socket socket, String message) throws IOException {
        String msgReply = "Hello from Server, we have " + listenersSocket.size() + " listeners."
                + "Message from leader is: " + message;
        OutputStream outputStream = null;
        LogUtil.logDebugToConsole("listenersocketsize: "+listenersSocket.size());
        try {
            outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(msgReply);
            printStream.close();
            PrintWriter out = new PrintWriter(outputStream,true);
            out.println(msgReply);

            } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
        }

        */


    private class SendReplyWithFileToSocketsThread extends  Thread{

        private List<Socket> hostSockets;
        private InputStream fileInputStream;
        SendReplyWithFileToSocketsThread(List<Socket> listenersSocket, InputStream fileIS, DATA_TYPE type) {
            this.hostSockets = listenersSocket;
            this.fileInputStream = fileIS;
        }

        @Override
        public void run() {
            for(Socket hostSocket:hostSockets) {
                try(OutputStream outputStream = hostSocket.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
//                    IOUtils.copy(fileInputStream,outputStream);
                   Utils.copyBetweenStreams(fileInputStream,outputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}


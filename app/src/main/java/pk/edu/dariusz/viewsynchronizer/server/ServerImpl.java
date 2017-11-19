package pk.edu.dariusz.viewsynchronizer.server;

import android.net.Network;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;

/**
 * Created by dariusz on 11/15/17.
 */

public class ServerImpl implements ServerViewSynchronizer {
    private ServerSocket serverSocketListener;
    private String message = "Default data from leader :)";
    private int serverPort;
    private List<Socket> listenersSocket;

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

    public void updateMessageForListeners(String message) {
        LogUtil.logInfoToConsole("Updating message from: "+this.message+" to: " +message);
        this.message = message;
        if(listenersSocket != null && listenersSocket.size() > 0) {
            Thread socketServerReplyThread = new SendMessageToAllListenersThread(listenersSocket);
            socketServerReplyThread.run();
        }else{
            LogUtil.logInfoToConsole("No listeners connected to server.");
        }
    }
    public void closeServer() {

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

        int connectionCount = 0;

        @Override
        public void run() {
            try {
                serverSocketListener = new ServerSocket(serverPort);
                listenersSocket = new ArrayList<>();
                while (true) {
                    // block the call until connection is created and return
                    // Socket object
                    LogUtil.logDebugToConsole("ServerIsListening now on");
                    Socket socket = serverSocketListener.accept();
                    LogUtil.logDebugToConsole("Servect connect wtih " +socket);
                    listenersSocket.add(socket);
                    connectionCount++;
                    new SendReplyWithCurrentMessageThread(socket).start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SendMessageToAllListenersThread extends Thread {

        private List<Socket> hostSockets;


        SendMessageToAllListenersThread(List<Socket> sockets) {
            this.hostSockets = sockets;
        }

        @Override
        public void run() {
            for (Socket socket : hostSockets) {
                try {
                    if(socket.isClosed()) {
                        hostSockets.remove(socket);
                    }
                    else {
                        sendMessageToSocket(socket, message);
                    }
                } catch (IOException e) {
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
                // TODO Auto-generated catch block
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


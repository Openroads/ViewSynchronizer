package pk.edu.dariusz.viewsynchronizer.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dariusz on 11/15/17.
 */

public class ClientDataSynchronizer {
    private Socket serverSocket;
    private String response="";
    private BufferedReader in;

    public ClientDataSynchronizer(String address, int port) throws IOException {
        serverSocket = new Socket(address,port);
        in = new BufferedReader(new InputStreamReader(
                serverSocket.getInputStream()));
    }
    public void closeSynchronizer() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }
    public String fetchDataFromServer() throws IOException {
           /* ByteArrayOutputStream byteArrayOutputStream=null;
            InputStream inputStream=null;
            try {
                byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                inputStream = serverSocket.getInputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            }finally {
                if(byteArrayOutputStream !=null)    byteArrayOutputStream.close();

            }*/
            response = in.readLine();

        return response;
    }
}

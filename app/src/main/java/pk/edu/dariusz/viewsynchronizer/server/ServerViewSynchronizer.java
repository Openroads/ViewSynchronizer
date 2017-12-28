package pk.edu.dariusz.viewsynchronizer.server;

import android.os.Message;

import java.net.ServerSocket;

import pk.edu.dariusz.viewsynchronizer.server.model.DataObjectToSend;

/**
 * Created by dariusz on 11/15/17.
 */

public interface ServerViewSynchronizer {

    ServerSocket runServer();
    void closeServer();
    int getPort();
    String getIpAddress();


    void updateMessageForListeners(DataObjectToSend message, Message msg);

}

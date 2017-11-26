package pk.edu.dariusz.viewsynchronizer.server;

import java.net.ServerSocket;

/**
 * Created by dariusz on 11/15/17.
 */

public interface ServerViewSynchronizer {

    ServerSocket runServer();
    void closeServer();
    int getPort();
    String getIpAddress();


    void updateMessageForListeners(DataObjectToSend message);

}

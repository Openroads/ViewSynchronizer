package pk.edu.dariusz.viewsynchronizer.server.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.widget.Toast;

import pk.edu.dariusz.viewsynchronizer.server.ServerViewSynchronizerImpl;
import pk.edu.dariusz.viewsynchronizer.server.ServerViewSynchronizer;
import pk.edu.dariusz.viewsynchronizer.server.model.DataObjectToSend;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.utils.ViewSynchronizerConstants;

/**
 * Created by dariusz on 11/15/17.
 */

public class ServerService extends Service {
    public static final int SEND_NEW_DATA_TO_LISTENERS = 1;
    private ServerViewSynchronizer viewSynchronizerServer;
    private static final int serverSocketPort = ViewSynchronizerConstants.APPLICATION_PORT;


    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_NEW_DATA_TO_LISTENERS:
                    DataObjectToSend dataObjectToSend = (DataObjectToSend) msg.obj;
                    viewSynchronizerServer.updateMessageForListeners(dataObjectToSend);
                    break;
             default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        super.onCreate();
        viewSynchronizerServer = new ServerViewSynchronizerImpl(serverSocketPort);
        viewSynchronizerServer.runServer();
        LogUtil.logDebugToConsole("ServerService - onCreate()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        LogUtil.logDebugToConsole("ServerService - onBind()");
        Toast.makeText(getApplicationContext(), "binding with server", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.logDebugToConsole("ServerService - onStartCommand()");
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.logDebugToConsole("onDestroy() server service");
        viewSynchronizerServer.closeServer();
    }

    public int getServerSocketPort() {
        return viewSynchronizerServer.getPort();
    }
}

package pk.edu.dariusz.viewsynchronizer.client.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import pk.edu.dariusz.viewsynchronizer.R;
import pk.edu.dariusz.viewsynchronizer.client.ClientDataSynchronizer;
import pk.edu.dariusz.viewsynchronizer.client.model.LeaderDataObject;
import pk.edu.dariusz.viewsynchronizer.commons.REQUEST_TYPE;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.commons.ServerDisconnected;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;

/**
 * Created by dariusz on 11/15/17.
 */

public class ClientViewSynchronizerService extends Service {
    // Binder given to clients
    private LeaderDataObject data;
    private File tempDirectoryForData;
    private final IBinder mBinder = new LocalBinder();
    private String address;
    private int port;
    private ClientDataSynchronizer clientDataSynchronizer;

    private LeaderDataObject nextLeaderDataObject = new LeaderDataObject();

    public class LocalBinder extends Binder {
        public ClientViewSynchronizerService getService() {
            // Return instance of Service to enable  clients  call public methods
            return ClientViewSynchronizerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        data = new LeaderDataObject(getString(R.string.default_waiting_message));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.logDebugToConsole("binding with clientsynchronizer service");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.logDebugToConsole("On start service cliensynchronizer");
        address="192.168.43.1";
        port=6000;
        tempDirectoryForData = new File(getFilesDir(),"file_from_leader");
        if(clientDataSynchronizer==null) new NewDataOnSocketCheckerThread().start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if(clientDataSynchronizer!=null)
                clientDataSynchronizer.closeSynchronizer();
                clientDataSynchronizer=null;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(tempDirectoryForData!=null){
                Utils.removeDirectoryOrFile(tempDirectoryForData);

            }
        }
        super.onDestroy();
    }

    public int checkForNewData(){
        return nextLeaderDataObject.getDownloadProgressAtomic().get();
    }

    public LeaderDataObject getNewData() throws ServerDisconnected {
        nextLeaderDataObject.getDownloadProgressAtomic().set(0);
        return data;
    }
    public LeaderDataObject getCurrentData(){
        return  data;
    }


    private class NewDataOnSocketCheckerThread extends Thread{

        @Override
        public void run() {
            try {
                String findAddress = Utils.checkHostsInLANForServerIp();
                if(!findAddress.equals(""))
                    address= findAddress;

                clientDataSynchronizer = new ClientDataSynchronizer(address,port,tempDirectoryForData);

                REQUEST_TYPE type = REQUEST_TYPE.FIRST;
                while(true) {
                    if(nextLeaderDataObject.getDownloadProgressAtomic().get()!=100) {
                        nextLeaderDataObject = new LeaderDataObject();
                        data = clientDataSynchronizer.fetchDataFromServer(type, nextLeaderDataObject);
                        nextLeaderDataObject.getDownloadProgressAtomic().set(100);
                        type = data.isDataFileCorrect() ? REQUEST_TYPE.GET_NEXT : REQUEST_TYPE.REFRESH;
                        LogUtil.logInfoToConsole("Checksum: " + data.isDataFileCorrect());
                    }
                }
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Exception in synchronizer loop ",e);
                e.printStackTrace();
            }
        }
    }
}

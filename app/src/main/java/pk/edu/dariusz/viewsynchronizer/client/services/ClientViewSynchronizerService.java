package pk.edu.dariusz.viewsynchronizer.client.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

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
    private LeaderDataObject data = new LeaderDataObject("Waiting...");
    private File tempDirectoryForData;
    int downloadProgress;
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding with clientsynchro", Toast.LENGTH_SHORT).show();

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //to enable for user choose address of server and port to connection(connection additional settings)
        /*
        address = intent.getStringExtra("serverAddress");
        port = intent.getIntExtra("serverPort",6000);
        */
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
//        return nextLeaderDataObject.getDownloadProgress();
        return nextLeaderDataObject.getDownloadProgressAtomic().get();
    }

    public LeaderDataObject getNewData() throws ServerDisconnected {
        nextLeaderDataObject.getDownloadProgressAtomic().set(0);
        return data;

/*        if(nextleaderDataObject.getDownloadProgress()==100) {
            nextleaderDataObject.setDownloadProgress(0);
            return data;
        }else if(nextleaderDataObject.getDownloadProgress() == -1){
            throw new ServerDisconnected("Leader has  switched off the server");
        }else
            return null;*/
/*
      if(downloadProgress==100) {
            downloadProgress = 0;
            return data;
        }else if(downloadProgress == -1){
            throw new ServerDisconnected("Leader has  switched off the server");
        }else
            return null;
*/


        /*       if(areNewData==0)
            return null;
        else if(areNewData==1) {
            areNewData=0;
            downloadProgress=0;
            return data;
        }else {
            areNewData=0;//TODO ?
            throw new ServerDisconnected("Leader has  switched off the server");
        }*/
    }
    public LeaderDataObject getCurrentData(){
        return  data;
    }


    private class NewDataOnSocketCheckerThread extends Thread{

        @Override
        public void run() {
            try {
                /*String findAddress = Utils.checkHostsInLANForServerIp();
                if(!findAddress.equals(""))
                    address= findAddress;*/

                clientDataSynchronizer = new ClientDataSynchronizer(address,port,tempDirectoryForData);

                REQUEST_TYPE type = REQUEST_TYPE.FIRST;
                while(true) {
                   // clientDataSynchronizer.connect();
                    if(nextLeaderDataObject.getDownloadProgressAtomic().get()!=100) {
                        nextLeaderDataObject = new LeaderDataObject();
                        downloadProgress = 1;
                        data = clientDataSynchronizer.fetchDataFromServer(type, nextLeaderDataObject);
                        nextLeaderDataObject.getDownloadProgressAtomic().set(100);
                        type = data.isDataFileCorrect() ? REQUEST_TYPE.GET_NEXT : REQUEST_TYPE.REFRESH;
                        LogUtil.logInfoToConsole("Checksum: " + data.isDataFileCorrect());
                    }
                }
            } catch (IOException e) {
                downloadProgress=-1;
                LogUtil.logErrorToConsole("Synchronizer loop",e);
                e.printStackTrace();
            }
        }
    }
/*
    public File fetchFileFromServer() {
        new FetchFileFromServerThread().start();
        return file;
    }
   private class FetchFileFromServerThread extends Thread{

        @Override
        public void run() {
            try {
                *//*String findAddress = Utils.checkHostsInLANForServerIp();
                if(!findAddress.equals(""))
                    address= findAddress;*//*
                clientDataSynchronizer = new ClientDataSynchronizer(address,port);

                    file = clientDataSynchronizer.fetchFileFromServer();
                    areNewData = 1;


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}

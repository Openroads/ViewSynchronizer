package pk.edu.dariusz.viewsynchronizer.client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

/**
 * Created by dariusz on 11/15/17.
 */

public class ClientViewSynchronizerService extends Service {
    // Binder given to clients
    private LeaderDataObject data = new LeaderDataObject("Waiting...");
    private File file;
    boolean areNewData = false;
    private final IBinder mBinder = new LocalBinder();
    private String address;
    private int port;
    private ClientDataSynchronizer clientDataSynchronizer;



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
        address="192.168.1.106";
        port=6000;
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
        }
        super.onDestroy();
    }

    public LeaderDataObject checkForNewData(){
        if(areNewData) {
            areNewData=false;
            return data;
        }else {
            return null;
        }

    }
    public LeaderDataObject getCurrentData(){
        return  data;
    }
    public File fetchFileFromServer() {
        new FetchFileFromServerThread().start();
        return file;
    }
    private class NewDataOnSocketCheckerThread extends Thread{

        @Override
        public void run() {
            try {
                /*String findAddress = Utils.checkHostsInLANForServerIp();
                if(!findAddress.equals(""))
                    address= findAddress;*/

               while(true) {
                    clientDataSynchronizer = new ClientDataSynchronizer(address,port);
                    data = clientDataSynchronizer.fetchDataFromServer();
                    LogUtil.logInfoToConsole("Are new data :)");
                    areNewData = true;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class FetchFileFromServerThread extends Thread{

        @Override
        public void run() {
            try {
                /*String findAddress = Utils.checkHostsInLANForServerIp();
                if(!findAddress.equals(""))
                    address= findAddress;*/
                clientDataSynchronizer = new ClientDataSynchronizer(address,port);

                    file = clientDataSynchronizer.fetchFileFromServer();
                    areNewData = true;


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

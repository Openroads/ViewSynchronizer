package pk.edu.dariusz.viewsynchronizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.net.InetAddress;

import pk.edu.dariusz.viewsynchronizer.server.ServerService;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

public class LeaderActivity extends AppCompatActivity {

    private EditText editTextShareMessage;

    private Messenger mServiceMessenger;
    private Intent serviceIntent;
    private boolean mBounded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.logDebugToConsole("LeaderActivity onCreate()");
        setContentView(R.layout.activity_leader);
        editTextShareMessage = (EditText) findViewById(R.id.editTextToSend);
        serviceIntent = new Intent(this, ServerService.class);
        startService(serviceIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.logDebugToConsole("LeaderActivity onStart()");
        bindService(serviceIntent,mConnection, Context.BIND_AUTO_CREATE);
    }

    public void updateViewContentOnClick(View view) {
        LogUtil.logInfoToConsole("updateViewContentOnClick Clicked");
        if (!mBounded){
            bindService(serviceIntent,mConnection, Context.BIND_AUTO_CREATE);
        }
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, ServerService.MSG_SHARE_TO_ALL, 0, 0);
        msg.obj = editTextShareMessage.getText().toString();

        try {
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBounded) {
            LogUtil.logDebugToConsole("LeaderAvtivity unbindService");
            unbindService(mConnection);
            mBounded = false;
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mServiceMessenger = new Messenger(service);
            mBounded = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceMessenger = null;
            mBounded = false;
        }
    };

    public void switchOffOnClick(View view) {
        if(mBounded) {
            unbindService(mConnection);
            stopService(serviceIntent);
            mBounded=false;
        }
    }
}

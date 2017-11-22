package pk.edu.dariusz.viewsynchronizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import pk.edu.dariusz.viewsynchronizer.client.ClientViewSynchronizerService;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

public class JoinerActivity extends AppCompatActivity {
    private ClientViewSynchronizerService mService;
    private boolean mBound = false;
    private  Intent checkerServiceIntent;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joiner);
        textView= (TextView)findViewById(R.id.displayContent);
        checkerServiceIntent = new Intent(this, ClientViewSynchronizerService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(checkerServiceIntent);
        bindService(checkerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ClientViewSynchronizerService.LocalBinder binder = (ClientViewSynchronizerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            textView.setText(mService.getCurrentData());
            new Thread() {
                public void run() {
                    while(true) {
                            final String newData = mService.checkForNewData();
                            if(newData != null)
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        textView.setText(newData);
                                    }
                                });
                    }
                }
            }.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void unsubscribeOnClick(View view) {
        unbindService(mConnection);
        mBound=false;
        stopService(checkerServiceIntent);
    }
}

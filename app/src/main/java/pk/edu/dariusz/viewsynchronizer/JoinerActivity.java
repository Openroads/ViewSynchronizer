package pk.edu.dariusz.viewsynchronizer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import pk.edu.dariusz.viewsynchronizer.client.ClientViewSynchronizerService;
import pk.edu.dariusz.viewsynchronizer.client.LeaderDataObject;

public class JoinerActivity extends AppCompatActivity {
    private ClientViewSynchronizerService mService;
    private boolean mBound = false;
    private  Intent checkerServiceIntent;
    private TextView textView;
    private ImageView imageView;
    private File fileFromServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joiner);
        textView= (TextView)findViewById(R.id.displayContent);
        imageView= (ImageView) findViewById(R.id.imageFromServer);
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
            textView.setText(mService.getCurrentData().getMessage());
            new Thread() {
                public void run() {
                    while (true) {
                        final LeaderDataObject newData = mService.checkForNewData();
                        if (newData != null) {
                            switch (newData.getType()) {
                                case STRING_MSG:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            textView.setText(newData.getMessage());
                                        }
                                    });
                                    break;
                                case IMG:
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                            Bitmap bitmap = BitmapFactory.decodeFile(newData.getFile().getAbsolutePath(),bmOptions);
                                            //bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                                            imageView.setImageBitmap(bitmap);

//                                            imageView.setImageURI(Uri.fromFile(newData.getFile()));

                                            if (newData.getMessage() != null)
                                                textView.setText(newData.getMessage());

                                            imageView.invalidate();
                                        }
                                    });
                                    break;
                            }
                        }
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
        finish();
    }

    public void fetchFileFromServerOnClick(View view) {
        new Thread() {
            public void run() {

                    fileFromServer = mService.fetchFileFromServer();
                    while(true)
                    if(fileFromServer!=null) {
                        final Uri uri = Uri.fromFile(fileFromServer);
                        if (uri != null)
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                        imageView.setImageURI(uri);
                                }
                            });
                    }

            }
        }.start();
    }
}

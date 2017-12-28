package pk.edu.dariusz.viewsynchronizer.client.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import pk.edu.dariusz.viewsynchronizer.R;
import pk.edu.dariusz.viewsynchronizer.client.services.ClientViewSynchronizerService;
import pk.edu.dariusz.viewsynchronizer.client.model.LeaderDataObject;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.commons.ServerDisconnected;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;
import pk.edu.dariusz.viewsynchronizer.utils.ViewSynchronizerConstants;

public class JoinerActivity extends AppCompatActivity {
    private final int REQUEST_DIRECTORY = 112;
    private ClientViewSynchronizerService mService;
    private boolean mBound = false;
    private  Intent checkerServiceIntent;
    private ProgressBar downloadProgressBar;
    private TextView textView;
    private ImageView imageView;
    private Button dataLeaderOpener;
    private Button saveDataButton;
    private EditText fileNameEditText;
    private RelativeLayout downloadElementsInRelativeLayout;
    private LeaderDataObject leaderDataObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joiner);
        downloadProgressBar = (ProgressBar)findViewById (R.id.downloadProgressBar);
        textView= (TextView)findViewById(R.id.displayContent);
        imageView= (ImageView) findViewById(R.id.imageFromServer);
        dataLeaderOpener = (Button) findViewById(R.id.openFileFromLeaderButton);
        saveDataButton = (Button) findViewById(R.id.downloadButton);
        fileNameEditText = (EditText) findViewById(R.id.sharedFileNameFromLeaderET);
        downloadElementsInRelativeLayout = (RelativeLayout) findViewById(R.id.downloadElementsRelativeLayout);
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
                    try {
                        while (true) {

                            final int newDataProgressPercent = mService.checkForNewData();
                            if(newDataProgressPercent > 0 && newDataProgressPercent <100){
                               if(downloadElementsInRelativeLayout.getVisibility() != View.VISIBLE) {
                                   runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           downloadElementsInRelativeLayout.setVisibility(View.VISIBLE);
                                       }
                                   });
                               }
                               downloadProgressBar.setProgress(newDataProgressPercent);

                            }else if(newDataProgressPercent ==100) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadElementsInRelativeLayout.setVisibility(View.GONE);
                                    }
                                });

                                LeaderDataObject newData = mService.getNewData();

                                if (newData != null) {
                                    leaderDataObject = newData;
                                    switch (leaderDataObject.getType()) {
                                        case STRING_MSG:
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    textView.setText(leaderDataObject.getMessage());
                                                }
                                            });
                                            break;
                                        case IMG:
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                                    Bitmap bitmap = BitmapFactory.decodeFile(leaderDataObject.getFile().getAbsolutePath(), bmOptions);
                                                    //bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
                                                    imageView.setImageBitmap(bitmap);

                                                    if (leaderDataObject.getMessage() != null)
                                                        textView.setText(leaderDataObject.getMessage());

                                                    imageView.invalidate();

                                                }
                                            });
                                            setButtonsVisibility(false);
                                            break;
                                        case PDF:
                                        case OTHER:
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (leaderDataObject.getMessage() != null)
                                                        textView.setText(leaderDataObject.getMessage());
                                                }
                                            });
                                            setButtonsVisibility(true);
                                    }
                                }
                            }
                        }
                    }catch (ServerDisconnected serverDisconnected){
                        LogUtil.logInfoToConsole("Leader has switched off the server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopService(checkerServiceIntent);
                                finish();
                            }
                        });
                    }
                }
            }.start();
        }

        private void setButtonsVisibility(final boolean fileNameVisibility){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(leaderDataObject.isAllowedToDownload())
                        saveDataButton.setVisibility(View.VISIBLE);
                    else
                        saveDataButton.setVisibility(View.INVISIBLE);

                    if(fileNameVisibility) {
                        imageView.setImageDrawable(null);
                        fileNameEditText.setVisibility(View.VISIBLE);
                        fileNameEditText.setText(leaderDataObject.getOriginalFileName());
                    }
                    else
                        fileNameEditText.setVisibility(View.INVISIBLE);


                    dataLeaderOpener.setVisibility(View.VISIBLE);
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void saveFileOnClick(View view) {
        /*
        -----------------------FOR path choser------------------
        final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("DirChooserSample")
                .allowReadOnlyDirectory(true)
                .allowNewDirectoryNameModification(true)
                .build();

        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
        startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
        */

        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File applicationDownloadDirectory= new File(externalStorageDirectory+ File.separator+ViewSynchronizerConstants.APPLICATION_DOWNLOAD_DIRECTORY_NAME);
        boolean mkdir=true;
        if(!applicationDownloadDirectory.exists()){
            mkdir = applicationDownloadDirectory.mkdir();
        }
        if(mkdir) {
            File outFile = new File(applicationDownloadDirectory, leaderDataObject.getOriginalFileName());
            try {
                Utils.copyFile(leaderDataObject.getFile(), outFile);
                Toast.makeText(this, getString(R.string.succesfully_saved_toast_message)+" " +ViewSynchronizerConstants.APPLICATION_DOWNLOAD_DIRECTORY_NAME,Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                LogUtil.logErrorToConsole("Error during move file from internal to external storage.",e);
                Toast.makeText(this, R.string.cant_save_info,Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, R.string.cant_save_info,Toast.LENGTH_LONG).show();
        }
    }
    public void unsubscribeOnClick(View view) {
        unbindService(mConnection);
        mBound=false;
        stopService(checkerServiceIntent);
        finish();
    }

    public void openFileFromServerOnClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FilenameUtils.getExtension(leaderDataObject.getOriginalFileName()));
//        Uri data = Uri.fromFile(leaderDataObject.getFile());
        Uri data = Uri.parse("content://"+ ViewSynchronizerConstants.APP_PACKAGE_NAME+"/"+leaderDataObject.getFile().getName());
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(data, mime);
        startActivity(intent);
    }


}

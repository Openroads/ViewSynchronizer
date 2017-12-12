package pk.edu.dariusz.viewsynchronizer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import pk.edu.dariusz.viewsynchronizer.commons.DATA_TYPE;
import pk.edu.dariusz.viewsynchronizer.server.DataObjectToSend;
import pk.edu.dariusz.viewsynchronizer.server.ServerService;
import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

public class LeaderActivity extends AppCompatActivity {

    private EditText editTextShareMessage;
    private ImageView imageView;
    private Uri uri;
    private File sharedFile;
    private Messenger mServiceMessenger;
    private Intent serviceIntent;
    private boolean mBounded;


    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.logDebugToConsole("LeaderActivity onCreate()");
        setContentView(R.layout.activity_leader);
        editTextShareMessage = (EditText) findViewById(R.id.editTextToSend);
        imageView = (ImageView) findViewById(R.id.imageToSend);
        serviceIntent = new Intent(this, ServerService.class);
        startService(serviceIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.logDebugToConsole("LeaderActivity onStart()");
        bindService(serviceIntent,mConnection, Context.BIND_AUTO_CREATE);
    }

    public void updateViewContentOnClick(View view) throws FileNotFoundException {
        LogUtil.logInfoToConsole("updateViewContentOnClick Clicked");
        if (!mBounded){
            bindService(serviceIntent,mConnection, Context.BIND_AUTO_CREATE);
        }
        // Create and send a message to the service, using a supported 'what' value
        DataObjectToSend dataObjectToSend = new DataObjectToSend(editTextShareMessage.getText().toString());
        new SendDataThread(dataObjectToSend).start();

    }
    private class SendDataThread extends Thread{
        DataObjectToSend dataObjectToSend;

        SendDataThread(DataObjectToSend dataObjectToSend){
            this.dataObjectToSend=dataObjectToSend;
        }
        @Override
        public void run() {
            dataObjectToSend.setFileUri(uri);
            if(uri!=null) {
                dataObjectToSend.setFile(makeFileFromUri(uri));
                getInfoAboutFile(uri, dataObjectToSend);
            }
            Message msg = Message.obtain(null, ServerService.SEND_NEW_DATA_TO_LISTENERS, 0, 0);
            msg.obj=dataObjectToSend;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                LogUtil.logErrorToConsole("Problem with sending message to server service",e);
            }
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

    public void chooseFileOnClick(View view) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            if (resultData != null) {
                uri = resultData.getData();
                if (uri != null) {
                    LogUtil.logDebugToConsole( "Uri: " + uri.getPath());
                }else{
                    LogUtil.logDebugToConsole("Selected file uri is null.");
                }
                String type = resultData.getType();
                if(type !=null)
                imageView.setImageURI(uri);
            }
        }
    }
    private void getInfoAboutFile(Uri uri,DataObjectToSend dots){
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(uri,null, null, null, null);
        cursor.moveToFirst();
        dots.setLength(cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE)));
        dots.setFileName(cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(contentResolver.getType(uri));

        String extension = FilenameUtils.getExtension( dots.getFileName());
        if(extension.equals("")) dots.setFileName(dots.getFileName()+"."+type);
        if(type ==null) type=extension;
        if(type.endsWith("jpg") || type.endsWith("png")) dots.setType(DATA_TYPE.IMG);
        else if(dots.getFileName().endsWith("pdf")) {
            dots.setType(DATA_TYPE.PDF);
        } else dots.setType(DATA_TYPE.OTHER);
        cursor.close();

    }
    private File makeFileFromUri(Uri uri) {
        File copy = new File(getFilesDir(),"shareDataCopy");

        try (FileOutputStream fileOutputStream = new FileOutputStream(copy)) {
            IOUtils.copy(getContentResolver().openInputStream(uri),fileOutputStream);
        } catch (IOException e) {
            LogUtil.logErrorToConsole("Exception during making copy from uri ",e);
        }

        return copy;
    }

}

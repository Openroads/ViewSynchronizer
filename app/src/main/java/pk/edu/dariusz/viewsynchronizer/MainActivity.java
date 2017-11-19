package pk.edu.dariusz.viewsynchronizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.net.SocketException;

import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;
import pk.edu.dariusz.viewsynchronizer.utils.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void leadButtonOnclick(View view) {
        LogUtil.logDebugToConsole("leadButtonOnclick Clicked");
        Intent intent = new Intent(this, LeaderActivity.class);
        startActivity(intent);
    }

    public void joinButtonOnClick(View view) {
        LogUtil.logDebugToConsole("joinButtonOnclick Clicked");
        Intent intent = new Intent(this, JoinerActivity.class);
        startActivity(intent);
    }
}

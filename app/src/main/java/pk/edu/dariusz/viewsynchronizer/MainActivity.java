package pk.edu.dariusz.viewsynchronizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import pk.edu.dariusz.viewsynchronizer.utils.LogUtil;

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

package pk.edu.dariusz.viewsynchronizer.utils;

import android.util.Log;

/**
 * Created by dariusz on 11/15/17.
 */

public class LogUtil {

    public static void  logDebugToConsole(String logMsg){
        Log.d("VSLOG.DEBUG",logMsg);
    }
    public static void  logInfoToConsole(String logMsg){
        Log.i("VSLOG.INFO",logMsg);
    }
}

package spit.ecell.encrypto.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;

/**
 * Created by Samriddha on 19-03-2018.
 */

public class NetworkUtils {

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager != null ? manager.getActiveNetworkInfo() : null;
        return (networkInfo != null && networkInfo.isConnected());
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("https://www.google.com");
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            return false;
        }
    }

}

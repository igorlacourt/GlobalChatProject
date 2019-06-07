package app.lacourt.globalchatproject.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
Class from Varun Barad's Medium post
https://medium.com/code-better/check-network-connectivity-on-android-in-10-lines-60d10361b73
*/
public class ConnectivityHelper {
    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = false;
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && (activeNetwork.isConnected());
        }

        return isConnected;
    }
}
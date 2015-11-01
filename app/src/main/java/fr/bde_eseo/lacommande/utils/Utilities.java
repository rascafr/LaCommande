package fr.bde_eseo.lacommande.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Rascafr on 19/10/2015.
 */
public class Utilities {

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    // Use it only to check if device "could be" online
    // It returns true even if it's connected to a hotspot without account (cf ESEO's Wifi)
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /** Simple way to check if data is ok or not **/
    /** Returns false if data is null, length == 0 or begins with <!DOCTYPE>, true otherwise **/
    public static boolean isNetworkDataValid (String data) {
        return data != null && data.length() > 0 && !data.startsWith("<!DOCTYPE");
    }
}

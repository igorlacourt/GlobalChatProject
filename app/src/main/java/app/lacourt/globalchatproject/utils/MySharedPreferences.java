package app.lacourt.globalchatproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {
    static Context context;

    public static void init(Context context) {
        MySharedPreferences.context = context;
    }

    public static String getMessage() {
        SharedPreferences sharedPref = context.getSharedPreferences("sharedpref.message", Context.MODE_PRIVATE);
        String defaultValue = "No message.";
        String message = sharedPref.getString("message", defaultValue);
        return message;
    }

    public static void saveMessage(String message) {
        SharedPreferences sharedPref = context.getSharedPreferences("sharedpref.message", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("message", message);
        editor.commit();
    }

    public static void saveCreator(String creator) {
        SharedPreferences sharedPref = context.getSharedPreferences("sharedpref.message", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("creator", creator);
        editor.commit();
    }

    public static String getCreator() {
        SharedPreferences sharedPref = context.getSharedPreferences("sharedpref.message", Context.MODE_PRIVATE);
        String defaultValue = "";
        String creator = sharedPref.getString("creator", defaultValue);
        return creator;
    }
}

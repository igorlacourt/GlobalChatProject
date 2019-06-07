package app.lacourt.globalchatproject.utils;

import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {//extends NotificationExtenderService {

    //TODO exemplo
//    @Override
//        public void notificationReceived(OSNotification notification) {
//            JSONObject data = notification.payload.additionalData;
//            String customKey;
//
//            if (data != null) {
//                customKey = data.optString("customkey", null);
//                if (customKey != null)
//                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
//            }
//        }
    @Override
    public void notificationReceived(OSNotification notification) {
        Log.d("WidgetLog", "notificationReceived called");

//        JSONObject data = notification.payload.additionalData;
        String creator = "";
        String message = "nada";

        JSONObject jsonObject = notification.toJSONObject();
        try {
            JSONObject payload = jsonObject.getJSONObject("payload");
            creator = payload.getString("title");
            message = payload.getString("body");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        String message = "nada";
//        message = notification.payload.toString();
        Log.d("WidgetLog", "message: " + message);

        MySharedPreferences.saveCreator(creator);
        MySharedPreferences.saveMessage(message);

        Log.d("WidgetLog", "edit.commit() called.");

        WidgetUpdater.update(creator, message);

    }

}
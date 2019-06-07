package app.lacourt.globalchatproject.utils;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotification {
    public SendNotification(String message, String heading, String notificationKey) {
        try {
//            notificationKey = "d197320d-f8b2-4e03-9c2f-30ea08bb8dd0";
            //TODO change json to send the notification to a certain tag.
            JSONObject notificationContent = new JSONObject(
                    "{" +
                            "'contents':{'en':'" + message + "'}," +
                            "'include_player_ids':['" + notificationKey + "']," +
                            "'headings':{'en': '" + heading + "'}" +
                            "}");
            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

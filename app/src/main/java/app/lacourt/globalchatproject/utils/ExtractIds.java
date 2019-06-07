package app.lacourt.globalchatproject.utils;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.Arrays;
import java.util.List;

public class ExtractIds {

    public static String[] ids;

    public static List<String> extractIds(DataSnapshot dataSnapshot) {
        String value = dataSnapshot.getValue().toString();
        Log.d("createChat()", "value = " + value);

        String cleanString = value.replace("{", "")
                .replace("}", "")
                .replace("=true", "")
                .replace(" ", "");

        ids = cleanString.split(",");

        for(int i = 0; i < ids.length; i++ ){
            Log.d("createChat()", "ids[" + i + "] = " + ids[i] + "\n");
        }

        List<String> idsList = Arrays.asList(ids);
        return  idsList;

    }
}

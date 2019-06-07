package app.lacourt.globalchatproject.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.widget.SimpleWidgetProvider;

public class WidgetUpdater {
    private static Context context;

    public static void init(Context context) {
        WidgetUpdater.context = context;
    }

    public static void update(String creator, String message) {
        Log.d("WidgetLog", "updateMyWidget called.");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        ComponentName thisWidget = new ComponentName(context, SimpleWidgetProvider.class);

        remoteViews.setTextViewText(R.id.tv_widget,  message);
        remoteViews.setTextViewText(R.id.tv_creator,  creator);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }
}

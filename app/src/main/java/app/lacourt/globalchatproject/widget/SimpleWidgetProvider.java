package app.lacourt.globalchatproject.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import app.lacourt.globalchatproject.R;
import app.lacourt.globalchatproject.ui.MainScreenActivity;
import app.lacourt.globalchatproject.utils.MySharedPreferences;


public class SimpleWidgetProvider extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d("WidgetLog", "SimpleWidgetProvider, updateAppWidget called.");
        Intent mainActivityIntent = new Intent(context, MainScreenActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        String creator = MySharedPreferences.getCreator();
        String message = MySharedPreferences.getMessage();

        views.setTextViewText(R.id.tv_creator, creator);
        views.setTextViewText(R.id.tv_widget, message);
        views.setOnClickPendingIntent(R.id.ly_widget, mainActivityPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("WidgetLog", "SimpleWidgetProvider, onUpdate called.");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.d("WidgetLog", "SimpleWidgetProvider, onUpdate got in to the loop.");
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void update() {

    }
}
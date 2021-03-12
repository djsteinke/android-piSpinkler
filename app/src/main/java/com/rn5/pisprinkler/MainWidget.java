package com.rn5.pisprinkler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import static com.rn5.pisprinkler.MainActivity.getFTempString;
import static com.rn5.pisprinkler.MainActivity.getTempString;

public class MainWidget extends AppWidgetProvider {
    private static final String TAG = MainWidget.class.getSimpleName();
    private static final String ACTION = "com.rn5.action.GARAGE_DOOR_CLICK";
    private Context context;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION);
        views.setOnClickPendingIntent(R.id.widget_text, PendingIntent.getBroadcast(context, 0, intent, 0));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);

            Intent intent = new Intent(context, getClass());
            views.setOnClickPendingIntent(R.id.widget_text, PendingIntent.getBroadcast(context, 0, intent, 0));


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

            UrlAsync async = new UrlAsync().forWidget(views, appWidgetManager, appWidgetId);
            async.execute("GET");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive(" + intent.getAction() + ")");
    }

}

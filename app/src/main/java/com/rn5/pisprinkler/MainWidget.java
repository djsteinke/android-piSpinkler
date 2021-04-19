package com.rn5.pisprinkler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
    private static final String ACTION = "com.rn5.action.UPDATE_TEMP";
    private static final String APP_WIDGET_ID = "com.rn5.extra.APP_WIDGET_ID";

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

            Intent intent = new Intent(context, MainWidget.class);
            intent.setAction(ACTION);
            Bundle bundle = new Bundle();
            bundle.putInt(APP_WIDGET_ID, appWidgetId);
            intent.putExtras(bundle);
            views.setOnClickPendingIntent(R.id.widget_text, PendingIntent.getBroadcast(context, appWidgetId, intent, 0));

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Bundle bundle = intent.getExtras();
        int appWidgetId = -1;
        if (bundle != null)
            appWidgetId = bundle.getInt(APP_WIDGET_ID, -1);
        if (appWidgetId > 0) {
            UrlAsync async = new UrlAsync().forWidget(views, appWidgetManager, appWidgetId);
            async.execute("GET", "getTemp");
        }
        Log.d(TAG, "onReceive(" + intent.getAction() + ") appWidgetId(" + appWidgetId + ")");
    }

}

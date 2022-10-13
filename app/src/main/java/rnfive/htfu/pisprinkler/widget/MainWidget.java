package rnfive.htfu.pisprinkler.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import rnfive.htfu.pisprinkler.MainActivityPiSprinkler;
import rnfive.htfu.pisprinkler.R;

import androidx.annotation.NonNull;

public class MainWidget extends AppWidgetProvider {
    private static final String TAG = MainWidget.class.getSimpleName();
    private static final String ACTION = "rnfive.htfu.action.UPDATE_TEMP";
    private static final String APP_WIDGET_ID = "rnfive.htfu.extra.APP_WIDGET_ID";
    private static double temp = 0.0d;
    private static int humidity = 0;

    private static boolean tempSet = false;
    private static boolean humiditySet = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //   CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        //   views.setTextViewText(R.id.appwidget_text, widgetText);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("sprinkler");

        databaseReference.child("temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temp = snapshot.getValue(Double.class);
                tempSet = true;
                updateValues(context, appWidgetManager, appWidgetId);
                Log.d(TAG, "onDataChange: temperature: " + temp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child("humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                humidity = snapshot.getValue(Integer.class);
                humiditySet = true;
                updateValues(context, appWidgetManager, appWidgetId);
                Log.d(TAG, "onDataChange: humidity: " + humidity);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static void updateValues(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId) {

        if (tempSet && humiditySet) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);

            String widgetText = MainActivityPiSprinkler.getFTempString(temp) + "\n" + humidity + "%";
            views.setTextViewText(R.id.widget_text, widgetText);

            Intent intent = new Intent(context, MainActivityPiSprinkler.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            views.setPendingIntentTemplate(R.id.widget_text, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            tempSet = false;
            humiditySet = false;
        }
    }

}

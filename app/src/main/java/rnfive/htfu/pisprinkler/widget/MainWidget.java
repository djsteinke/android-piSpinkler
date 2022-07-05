package rnfive.htfu.pisprinkler.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import rnfive.htfu.pisprinkler.MainActivity;
import rnfive.htfu.pisprinkler.R;

import androidx.annotation.NonNull;

public class MainWidget extends AppWidgetProvider {
    private static final String TAG = MainWidget.class.getSimpleName();
    private static final String ACTION = "rnfive.htfu.action.UPDATE_TEMP";
    private static final String APP_WIDGET_ID = "rnfive.htfu.extra.APP_WIDGET_ID";
    String tempStr = "0.0";
    String humidityStr = "0%";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled()");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);

        /*
        getValues(views);
        Intent intent = new Intent(context, getClass());
        intent.setAction(ACTION);
        views.setOnClickPendingIntent(R.id.widget_main, PendingIntent.getBroadcast(context, 0, intent, 0));
         */

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider

        Log.d(TAG, "onUpdate()");
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);

            FirebaseApp.initializeApp(context);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("sprinkler");

            databaseReference.child("temperature").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double temp = snapshot.getValue(Double.class);
                    tempStr = MainActivity.getFTempString(temp);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            databaseReference.child("humidity").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double humidity = snapshot.getValue(Double.class);
                    humidityStr = Math.round(humidity) + "%";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            views.setTextViewText(R.id.widget_temp, tempStr);
            views.setTextViewText(R.id.widget_hunidity, humidityStr);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            /*
            Intent intent = new Intent(context, MainWidget.class);
            intent.setAction(ACTION);
            Bundle bundle = new Bundle();
            bundle.putInt(APP_WIDGET_ID, appWidgetId);
            intent.putExtras(bundle);
            views.setOnClickPendingIntent(R.id.widget_main, PendingIntent.getBroadcast(context, appWidgetId, intent, 0));

             */

        }
    }

    /*
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Bundle bundle = intent.getExtras();
        int appWidgetId = -1;
        if (bundle != null)
            appWidgetId = bundle.getInt(APP_WIDGET_ID, -1);
        if (appWidgetId > 0) {
            //UrlAsync async = new UrlAsync().forWidget(views, appWidgetManager, appWidgetId);
            //async.execute("GET", "getTemp");
            float temp = 0.0f;
            int humidity = 0;
            final int widgetId = appWidgetId;
            //getValues(views);
        }
        Log.d(TAG, "onReceive(" + intent.getAction() + ") appWidgetId(" + appWidgetId + ")");
    }

     */

    /*
    private void getValues(RemoteViews views) {

        Log.d(TAG, "getValues()");
        databaseReference.child("temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double temp = snapshot.getValue(Double.class);
                String txt = MainActivity.getFTempString(temp);
                views.setTextViewText(R.id.widget_temp, txt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child("humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double temp = snapshot.getValue(Double.class);
                String txt = Math.round(temp) + "%";
                views.setTextViewText(R.id.widget_hunidity, txt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

     */

}

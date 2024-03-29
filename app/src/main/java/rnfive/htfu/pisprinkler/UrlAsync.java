package rnfive.htfu.pisprinkler;

import android.appwidget.AppWidgetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import rnfive.htfu.pisprinkler.define.Settings;
import rnfive.htfu.pisprinkler.listener.UrlResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class UrlAsync extends AsyncTask<String,Void,JSONObject> {
    private static final String TAG = UrlAsync.class.getSimpleName();
    private final Settings settings = Settings.fromFile();
    String urlString = "http://" + settings.getIp() + ":" + settings.getPort();
    private UrlResponseListener urlResponseListener;
    private RemoteViews remoteView;
    private AppWidgetManager appWidgetManager;
    private int appWidgetId;

    public UrlAsync() {
        Log.d(TAG, "UrlAsync()");
    }

    public UrlAsync withListener(UrlResponseListener listener) {
        this.urlResponseListener = listener;
        return this;
    }

    public UrlAsync forWidget(RemoteViews view, AppWidgetManager appWidgetManager, int appWidgetId) {
        this.remoteView = view;
        this.appWidgetId = appWidgetId;
        this.appWidgetManager = appWidgetManager;
        return this;
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        try {
            String finalUrl = urlString + "/" + urls[1];
            String logMsg = "doInBackground() " + urls[0] + " " + finalUrl;

            URL url = new URL(finalUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000);
            con.setConnectTimeout(10000);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            if (urls[0].equals("GET"))
                con.connect();
            else if (urls[0].equals("POST")) {
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                BufferedOutputStream out = new BufferedOutputStream(con.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(urls[2]);
                writer.flush();
                writer.close();
                out.close();
                con.connect();
            } else
                return null;
            logMsg += " Response[" + con.getResponseCode() + "]";
            Log.d(TAG,logMsg);
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            return new JSONObject(sb.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"doInBackground() ERROR: " + e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject val) {
        if (val != null) {
            if (urlResponseListener != null)
                urlResponseListener.onResponse(val);
            if (remoteView != null) {
                DecimalFormat df0 = new DecimalFormat("#");
                try {
                    if (val.getString("type").equals("temp")) {
                        JSONObject response = val.getJSONObject("response");
                        String txt = MainActivityPiSprinkler.getFTempString(response.getDouble("temp")) + "\n";
                        txt += df0.format(response.getDouble("humidity")) + "%";
                        remoteView.setTextViewText(R.id.widget_text, txt);
                        appWidgetManager.updateAppWidget(appWidgetId, remoteView);
                        Log.d(TAG, txt);
                    }
                } catch (JSONException | NullPointerException e) {
                    Log.e(TAG, "onResponse() ERROR: " + e.getMessage());
                }
            }
        }
    }
}


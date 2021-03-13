package com.rn5.pisprinkler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rn5.pisprinkler.define.Program;
import com.rn5.pisprinkler.define.Settings;
import com.rn5.pisprinkler.define.Zone;
import com.rn5.pisprinkler.listener.UrlResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.rn5.pisprinkler.MenuUtil.menuItemSelector;

public class MainActivity extends AppCompatActivity implements UrlResponseListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    TextView ipText;
    TextView portText;
    private TextView mainText;
    private FlexboxLayout flexboxLayout;
    private ConstraintLayout mainLayout;

    public static int port = 1983;
    public static String ip = "192.168.0.152";
    public static File file;
    private Settings settings;

    public static final List<Program> programs = new ArrayList<>();
    public static final List<Zone> zones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setTheme(R.style.Theme_PiSprinkler);
        setContentView(R.layout.activity_main);

        if (!setFilePath()) {
            Toast.makeText(this, "File Path Creation Failed.", Toast.LENGTH_SHORT).show();
        }
        settings = Settings.load();
        getSetup();

        mainLayout = findViewById(R.id.main_layout);
        flexboxLayout = findViewById(R.id.zone_flex_box);
        mainText = findViewById(R.id.main_text);
        ImageButton button = findViewById(R.id.button);
        ImageButton ipBtn = findViewById(R.id.ip_button);
        ImageButton portBtn = findViewById(R.id.port_button);
        ipText = findViewById(R.id.ip_text);
        portText = findViewById(R.id.port_text);
        ipText.setText(settings.getIp());
        portText.setText(String.format(Integer.toString(settings.getPort()), Locale.US));

        button.setOnClickListener(view -> click());

        ipBtn.setOnClickListener(view -> alert("Device IP:", ipBtn.getId()));

        portBtn.setOnClickListener(view -> alert("Port:", portBtn.getId()));
    }

    public void click() {
        Log.d(TAG,"click()");
        UrlAsync async = new UrlAsync().withListener(this);
        async.execute("GET","getTemp");
    }

    private void loadFlexBox() {
        for (Zone z : zones) {
            View v = getLayoutInflater().inflate(R.layout.fb_text_view,flexboxLayout, false);
            TextView tv = v.findViewById(R.id.fb_text);
            tv.setText(String.format(Locale.US,"%d",z.getZone()));
            flexboxLayout.addView(v);
        }
    }

    public void alert(final String title, final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        FrameLayout layout = new FrameLayout(this);

        final EditText input = new EditText(this);
        input.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        if (id == R.id.port_button)
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (id == R.id.ip_button)
            input.setText(ipText.getText().toString());
        layout.addView(input);
        final ViewGroup.MarginLayoutParams lpt =(ViewGroup.MarginLayoutParams)input.getLayoutParams();
        lpt.setMargins(75,lpt.topMargin,110,lpt.bottomMargin);
        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String val = input.getText().toString();
            if (id == R.id.ip_button) {
                ipText.setText(val);
                ip = val;
                settings.setIp(ip);
            } else {
                portText.setText(val);
                port = Integer.parseInt(val);
                settings.setPort(port);
            }
            settings.save();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean setFilePath() {
        file = this.getExternalFilesDir("Settings");
        boolean result = file != null && (file.exists() || file.mkdir());
        return result && (file.canWrite() || file.setWritable(true, true));
    }

    @Override
    public void onResponse(JSONObject val) {
        String txt = "";
        DecimalFormat df0 = new DecimalFormat("#");
        try {
            JSONObject setup = val.getJSONObject("setup");
            JSONArray jPrograms = setup.getJSONArray("programs");
            JSONArray jZones = setup.getJSONArray("zones");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
            Gson gson = gsonBuilder.create();
            for (int i=0; i < jZones.length(); i++) {
                JSONObject z = (JSONObject) jZones.get(i);
                Zone newZ = gson.fromJson(z.toString(), Zone.class);
                if (!zones.contains(newZ))
                    zones.add(newZ);
            }
            for (int i=0; i < jPrograms.length(); i++) {
                JSONObject p = (JSONObject) jPrograms.get(i);
                Program newP = gson.fromJson(p.toString(), Program.class);
                if (!programs.contains(newP))
                    programs.add(newP);
            }
            loadFlexBox();
            return;
        } catch (JSONException e) {
            Log.e(TAG, "onResponse() Error: " + e.getMessage());
        }
        try {
            txt = "cT: " + getTempString((double) val.getDouble("temp")) + "\n";
            txt += "cH: " + df0.format((double) val.getDouble("humidity")) + "%\n";
            txt += "aT: " + getTempString((double) val.getDouble("avg_temp")) + "\n";
            txt += "aH: " + df0.format((double) val.getDouble("avg_humidity")) + "%\n";
            txt += "\u02C4T: " + getTempString((double) val.getDouble("temp_max")) + "\n";
            txt += "\u02C5T: " + getTempString((double) val.getDouble("temp_min")) + "\n";
            if (mainText != null) {
                mainText.setText(txt);
            }
        } catch (JSONException e) {
            Log.e(TAG, "onResponse() Error: " + e.getMessage());
        }
    }

    private void getSetup() {
        UrlAsync async = new UrlAsync().withListener(this);
        async.execute("GET","getSetup");
    }

    public static String getTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#.#");
        return df1.format(c) + "\u00B0C [" + df1.format(getTempF(c)) + "\u00B0F]";
    }

    public static String getFTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#.#");
        return df1.format(getTempF(c)) + "\u00B0F";
    }

    public static double getTempF(double c) {
        return c*1.8 + 32;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = menuItemSelector(this, item, TAG);
        return result || super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        click();
    }
}
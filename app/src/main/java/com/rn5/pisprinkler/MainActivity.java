package com.rn5.pisprinkler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.nambimobile.widgets.efab.FabOption;
import com.rn5.pisprinkler.adapter.ProgramSwipeAdapter;
import com.rn5.pisprinkler.define.ProgramAlert;
import com.rn5.pisprinkler.listener.CreateListener;
import com.rn5.pisprinkler.define.Program;
import com.rn5.pisprinkler.define.Settings;
import com.rn5.pisprinkler.define.Zone;
import com.rn5.pisprinkler.define.ZoneAlert;
import com.rn5.pisprinkler.listener.UrlResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.rn5.pisprinkler.MenuUtil.menuItemSelector;
import static com.rn5.pisprinkler.define.Constants.formatInt;

public class MainActivity extends AppCompatActivity implements UrlResponseListener, CreateListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    TextView ipText;
    private TextView mainText;
    private FlexboxLayout flexboxLayout;

    public static int port = 1983;
    public static String ip = "192.168.0.152";
    public static File file;
    private Settings settings;
    private FabOption addZoneFab;
    private FabOption addProgramFab;

    public static final List<Program> programs = new ArrayList<>();
    public static final List<Zone> zones = new ArrayList<>();

    private ViewPager2 pager;
    private FragmentStateAdapter pagerAdapter;

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

        flexboxLayout = findViewById(R.id.zone_flex_box);
        mainText = findViewById(R.id.main_text);
        ImageButton button = findViewById(R.id.button);
        ImageButton ipBtn = findViewById(R.id.ip_button);
        ipText = findViewById(R.id.ip_text);
        String val = settings.getIp() + " : " + formatInt(settings.getPort());
        ipText.setText(val);

        button.setOnClickListener(view -> click());

        ipBtn.setOnClickListener(view -> alert());

        pager = findViewById(R.id.pager);
        pagerAdapter = new ProgramSwipeAdapter(this, programs.size());
        pager.setAdapter(pagerAdapter);
    }

    public void click() {
        Log.d(TAG,"click()");
        UrlAsync async = new UrlAsync().withListener(this);
        async.execute("GET","getTemp");
    }

    private void loadFlexBox() {
        flexboxLayout.removeAllViews();
        for (Zone z : zones) {
            View v = getLayoutInflater().inflate(R.layout.fb_text_view,flexboxLayout, false);
            TextView tv = v.findViewById(R.id.fb_text);
            String zoneId = formatInt(z.getZone()+1);
            int typeDr = R.drawable.head_fixed;
            switch (z.getType()) {
                case 1:
                    typeDr = R.drawable.head_rotary;
                    break;
                case 2:
                    typeDr = R.drawable.head_rotor;
                    break;
                default:
                    break;
            }
            v.setBackground(ContextCompat.getDrawable(this, typeDr));
            tv.setText(zoneId);
            tv.setOnClickListener(view -> ZoneAlert.getZoneAlert(this, null, null, z.getZone()));
            tv.setOnLongClickListener(view -> {ZoneAlert.getDeleteZoneAlert(this, null, this, z.getZone());
                return true;});
            flexboxLayout.addView(v);
        }
    }

    public void alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = LayoutInflater.from(builder.getContext()).inflate(R.layout.popup_device,null);
        final EditText etIp = v.findViewById(R.id.et_ip);
        final EditText etPort = v.findViewById(R.id.et_port);

        etIp.setText(settings.getIp());
        etPort.setText(formatInt(settings.getPort()));

        builder.setView(v);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String ip = etIp.getText().toString();
            String port = etPort.getText().toString();
            String val = ip + " : " + port;
            ipText.setText(val);
            settings.setIp(ip);
            settings.setPort(Integer.parseInt(port));
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
    public void onCreateZone() {
        loadFlexBox();
    }

    @Override
    public void onCreateProgram() {
        pagerAdapter = new ProgramSwipeAdapter(this, programs.size());
        pager.setAdapter(pagerAdapter);
    }

    @Override
    public void onResponse(JSONObject val) {
        String txt;
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
            this.onCreateProgram();
            return;
        } catch (JSONException | JsonSyntaxException e) {
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

    public void addZone(View v) {
        ZoneAlert.getZoneAlert(v.getContext(), null, this, zones.size());
    }

    public void addProgram(View v) {
        ProgramAlert alert = new ProgramAlert()
                .withListener(this)
                .withContext(this);
        ProgramAlert.getProgramAlert(alert, programs.size());
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean result = menuItemSelector(this, item, TAG);
        return result || super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        click();
    }
}
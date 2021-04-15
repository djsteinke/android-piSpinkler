package com.rn5.pisprinkler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.nambimobile.widgets.efab.FabOption;
import com.rn5.pisprinkler.adapter.ProgramSwipeAdapter;
import com.rn5.pisprinkler.define.History;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.rn5.pisprinkler.MenuUtil.menuItemSelector;
import static com.rn5.pisprinkler.define.Constants.formatInt;
import static com.rn5.pisprinkler.define.Constants.sdf;

public class MainActivity extends AppCompatActivity implements UrlResponseListener, CreateListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    TextView ipText;
    private TextView mainText;
    private FlexboxLayout flexboxLayout;
    private LinearLayout ll_dots;
    private ImageView iv_history;

    private static final int historyDays = 4;
    private int iHistW = 0;
    private int iHistH = 0;
    private int currProgram = 0;
    public static int port = 1983;
    public static String ip = "192.168.0.152";
    public static File file;
    private static Settings settings;
    private FabOption addZoneFab;
    private FabOption addProgramFab;
    private long lastRefresh = 0;

    public static final List<Program> programs = new ArrayList<>();
    public static final List<Zone> zones = new ArrayList<>();
    public static final List<History> history = new ArrayList<>();

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
        lastRefresh = Calendar.getInstance().getTimeInMillis();

        flexboxLayout = findViewById(R.id.zone_flex_box);
        mainText = findViewById(R.id.main_text);
        ImageButton button = findViewById(R.id.button);
        ipText = findViewById(R.id.ip_text);
        String val = settings.getIp() + ":" + formatInt(settings.getPort());
        ipText.setText(val);
        ll_dots = findViewById(R.id.ll_dots);
        iv_history = findViewById(R.id.history);
        ViewTreeObserver vto = iv_history.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                iv_history.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                iHistW  = iv_history.getMeasuredWidth();
                iHistH = iv_history.getMeasuredHeight();
                getHistory();
                //drawHistory();
            }
        });

        button.setOnClickListener(view -> click());

        pager = findViewById(R.id.pager);
        pagerAdapter = new ProgramSwipeAdapter(this, this, this, programs.size());
        pager.setAdapter(pagerAdapter);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currProgram = position;
                setLl_dots();
            }
        });
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

    public static void alert(Context context, CreateListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
            settings.setIp(ip);
            settings.setPort(Integer.parseInt(port));
            settings.save();
            if (listener != null)
                listener.onUpdateUrl();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean setFilePath() {
        file = this.getExternalFilesDir("Settings");
        boolean result = file != null && (file.exists() || file.mkdir());
        return result && (file.canWrite() || file.setWritable(true, true));
    }

    private void setLl_dots() {
        if (ll_dots.getChildCount() != programs.size()) {
            ll_dots.removeAllViews();
            for (int i=0;i<programs.size();i++) {
                ImageView iv = (ImageView) getLayoutInflater().inflate(R.layout.dot, ll_dots, false);
                iv.setActivated(i != currProgram);
                ll_dots.addView(iv);
            }
        } else {
            for (int i = 0; i < programs.size(); i++) {
                ImageView iv = (ImageView) ll_dots.getChildAt(i);
                iv.setActivated(i != currProgram);
            }
        }
    }

    private void saveProgram() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        Gson gson = gsonBuilder.create();
        UrlAsync async = new UrlAsync();
        async.execute("POST","update/programs", gson.toJson(programs));
    }

    @Override
    public void onUpdateProgram(int pPos) {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        if (allFragments != null) {
            for (Fragment fragment : allFragments) {
                ProgramFragment f1 = (ProgramFragment) fragment;
                if (f1.getPos() == pPos)
                    f1.updateProgram();
            }
        }
        saveProgram();
    }

    @Override
    public void onUpdateStep(int pPos) {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        if (allFragments != null) {
            for (Fragment fragment : allFragments) {
                ProgramFragment f1 = (ProgramFragment) fragment;
                if (f1.getPos() == pPos)
                    f1.updateSteps();
            }
        }
        saveProgram();
    }

    @Override
    public void onUpdateUrl() {
        String val = settings.getIp() + ":" + formatInt(settings.getPort());
        ipText.setText(val);
    }

    @Override
    public void onCreateZone() {
        loadFlexBox();
    }

    @Override
    public void onCreateProgram(boolean save) {
        pagerAdapter = new ProgramSwipeAdapter(this, this, this, programs.size());
        pager.setAdapter(pagerAdapter);
        setLl_dots();
        if (save)
            saveProgram();
    }

    @Override
    public void onResponse(JSONObject val) {
        try {
            String type = val.getString("type");
            JSONObject response = val.getJSONObject("response");
            switch (type) {
                case "setup":
                    loadSetup(response);
                    break;
                case "temp":
                    loadTemp(response);
                    break;
                case "history":
                    loadHistory(response);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "onResponse() Invalid Message " + e.getMessage());
        }
    }

    private float getPxFromDp(Float dip) {
        Resources r = getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }

    private double[] getMaxMin() {
        double[] mm = new double[] {0, 0};
        for (History h : history) {
            if (mm[0] == 0 || mm[0] < h.getTMax()) {
                mm[0] = h.getTMax();
            }
            if (mm[1] == 0 || mm[1] > h.getTMin()) {
                mm[1] = h.getTMin();
            }
        }
        mm[0] = getTempF(mm[0]);
        mm[1] = getTempF(mm[1]);
        int x = (int) mm[1] - (mm[1]>=0?0:10);
        Log.d(TAG, "getMaxMin " + mm[0] + " " + mm[1]);
        mm[1] = (double) ((x/10)*10);
        //mm[1] = (double) (x-(5-Math.abs(x%5)));
        x = (int) mm[0] + 10;
        mm[0] = (double) ((x/10)*10);
        //mm[0] = (double) (x+(5-x%5));
        Log.d(TAG, "getMaxMin " + mm[0] + " " + mm[1]);
        return mm;
    }

    private long getMinMs(int d) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return cal.getTimeInMillis();
    }

    private List<History.Temp> getTempList() {
        List<History.Temp> ret = new ArrayList<>();
        for (History h : history) {
            Log.d("getTempList()", sdf.format(h.getDt()));
            ret.addAll(h.getHistory());
        }
        Collections.sort(ret, History.Temp.comparator);
        return ret;
    }

    private int getThemeColor(int val) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(val, typedValue, true);
        return typedValue.data;
    }

    private List<Double> getAvg(List<Double> list, double val) {
        if (list.size() > 0)
            list.remove(list.size()-1);
        list.add(0, val);
        if (list.size() > 3)
            list.remove(list.size()-1);
        double tot = 0;
        for (Double d:list) {
            tot += d;
        }
        list.add(tot/(double)list.size());

        return list;
    }

    private void drawHistory() {
        Log.d(TAG, "drawHistory() x["+ iHistW + "] + y[" + iHistH + "]");
        Bitmap bitmap = Bitmap.createBitmap(iHistW, iHistH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int xOff = 80;
        long minMs = getMinMs(historyDays-1);
        double[] mm = getMaxMin();
        double xMs = (double)(iHistW-xOff)/(historyDays*24*3600*1000);
        double yD = (double)iHistH/(mm[0]-mm[1]);

        Paint pTemp = new Paint();
        pTemp.setStyle(Paint.Style.STROKE);
        pTemp.setColor(getThemeColor(R.attr.colorSecondary));
        pTemp.setStrokeWidth(getPxFromDp(1.5f));

        Paint pH = new Paint();
        pH.setStyle(Paint.Style.STROKE);
        pH.setColor(getColor(R.color.gray));
        pH.setStrokeWidth(1);
        pH.setTextSize(getPxFromDp(10f));

        Paint pTxt = new Paint();
        pTxt.setStyle(Paint.Style.FILL);
        pTxt.setColor(getColor(R.color.gray));
        pTxt.setStrokeWidth(1);
        pTxt.setTextSize(getPxFromDp(15f));
        pTxt.setTextAlign(Paint.Align.RIGHT);

        List<Double> tAvg = new ArrayList<>();
        int i = (int) mm[0] - 10;

        while (i > mm[1]) {
            double y = (mm[0]-i)*yD;
            Path p = new Path();
            p.moveTo(xOff, (float)y);
            p.lineTo(iHistW, (float)y);
            canvas.drawPath(p, pH);
            canvas.drawText(formatInt(i)+"\u00B0", xOff-10, (float)y+getPxFromDp(5f), pTxt);
            i -= 10;
        }
        for (int d=0; d<historyDays-1; d++) {
            double x = xOff+(getMinMs(d)-minMs)*xMs;
            Path p = new Path();
            p.moveTo((float)x, 0);
            p.lineTo((float)x, iHistH);
            canvas.drawPath(p, pH);
        }

        Path pT = new Path();
        boolean move = true;
        for (History.Temp t : getTempList()) {
            getAvg(tAvg, t.getT());
            double tmp = tAvg.get(tAvg.size()-1);
            double x = xOff+(t.getTime().getTime()-minMs)*xMs;
            double fTmp = getTempF(tmp);
            double y = (mm[0]-fTmp)*yD;
            if (x >= xOff) {
                if (move)
                    pT.moveTo((float) x, (float) y);
                else
                    pT.lineTo((float) x, (float) y);
                move = false;
            }
        }

        canvas.drawPath(pT, pTemp);
        iv_history.setImageBitmap(bitmap);
    }

    private void loadHistory(JSONObject val) throws JSONException {

        JSONArray jHistory = val.getJSONArray("history");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(History.Temp.dateFormat);
        Gson gson = gsonBuilder.create();
        for (int i=0; i < jHistory.length(); i++) {
            JSONObject z = (JSONObject) jHistory.get(i);
            History newH = gson.fromJson(z.toString(), History.class);
            if (!history.contains(newH))
                history.add(newH);
        }
        Log.d(TAG, "loadHistory() " + history);
        drawHistory();
        lastRefresh = Calendar.getInstance().getTimeInMillis();
    }

    private void loadTemp(JSONObject val) throws JSONException {
        DecimalFormat df0 = new DecimalFormat("#");

        String txt = "cT: " + getTempString(val.getDouble("temp")) + "\n";
        txt += "cH: " + df0.format(val.getDouble("humidity")) + "%\n";
        txt += "aT: " + getTempString(val.getDouble("avg_temp")) + "\n";
        txt += "aH: " + df0.format(val.getDouble("avg_humidity")) + "%\n";
        txt += "\u02C4T: " + getTempString(val.getDouble("temp_max")) + "\n";
        txt += "\u02C5T: " + getTempString(val.getDouble("temp_min")) + "\n";

        if (mainText != null) {
            mainText.setText(txt);
        }
    }

    private void loadSetup(JSONObject val) throws JSONException {
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
        this.onCreateProgram(false);
    }

    private void getSetup() {
        UrlAsync async = new UrlAsync().withListener(this);
        async.execute("GET","getSetup");
    }

    private void getHistory() {
        UrlAsync async = new UrlAsync().withListener(this);
        async.execute("GET","getTemp/" + historyDays);
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
        boolean result = menuItemSelector(this, item, this, TAG);
        return result || super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        click();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastRefresh < (Calendar.getInstance().getTimeInMillis()-(15f*60*1000)))
            getHistory();
    }
}
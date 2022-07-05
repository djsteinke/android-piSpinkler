package rnfive.htfu.pisprinkler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import rnfive.htfu.pisprinkler.define.Constants;
import rnfive.htfu.pisprinkler.define.Current;
import rnfive.htfu.pisprinkler.define.History;
import rnfive.htfu.pisprinkler.define.Program;
import rnfive.htfu.pisprinkler.define.ProgramAlert;
import rnfive.htfu.pisprinkler.define.Settings;
import rnfive.htfu.pisprinkler.define.Setup;
import rnfive.htfu.pisprinkler.define.Zone;
import rnfive.htfu.pisprinkler.listener.CreateListener;
import rnfive.htfu.pisprinkler.listener.UrlResponseListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
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
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rnfive.htfu.pisprinkler.R;

import rnfive.htfu.pisprinkler.adapter.ProgramSwipeAdapter;
import rnfive.htfu.pisprinkler.define.StatusAlert;
import rnfive.htfu.pisprinkler.define.ZoneAlert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rnfive.htfu.pisprinkler.MenuUtil.menuItemSelector;

public class MainActivity extends AppCompatActivity implements UrlResponseListener, CreateListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView ipText;
    private TextView mainText;
    private FlexboxLayout flexboxLayout;
    private LinearLayout ll_dots;
    private ImageView iv_history;

    private static final int historyDays = 4;
    private int iHistW = 0;
    private int iHistH = 0;
    private int currProgram = 0;
    public static int port = 1984;
    public static String ip = "192.168.0.152";
    public static File file;
    private static Settings settings;
    private static Setup setup;
    private long lastRefresh = 0;

    public static List<Program> programs = new ArrayList<>();
    public static List<Zone> zones = new ArrayList<>();
    public static List<History> history = new ArrayList<>();
    public static Current current;

    private ViewPager2 pager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setTheme(R.style.Theme_PiSprinkler);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        if (!setFilePath()) {
            Toast.makeText(this, "File Path Creation Failed.", Toast.LENGTH_SHORT).show();
        }
        settings = Settings.fromFile();
        lastRefresh = Calendar.getInstance().getTimeInMillis();

        flexboxLayout = findViewById(R.id.zone_flex_box);
        mainText = findViewById(R.id.main_text);
        ImageButton button = findViewById(R.id.button);
        ipText = findViewById(R.id.ip_text);
        String val = settings.getIp() + ":" + Constants.formatInt(settings.getPort());
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

        getSetup();

        pager = findViewById(R.id.pager);
        pagerAdapter = new ProgramSwipeAdapter(this, programs.size(), this);
        pager.setAdapter(pagerAdapter);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currProgram = position;
                setLl_dots();
            }
        });

        setup = Setup.fromFile();
        if (setup.getId() != null)
            processSetup();
    }

    public void click() {
        Log.d(TAG,"click()");
        UrlAsync async = new UrlAsync().withListener(this);
        async.execute("GET","getTemp");
    }

    private void processSetup() {
        programs = setup.getPrograms();
        zones = setup.getZones();
        //history = setup.getHistories();
        loadFlexBox();
        onCreateProgram(false);
    }

    private void loadFlexBox() {
        flexboxLayout.removeAllViews();
        for (Zone z : zones) {
            View v = getLayoutInflater().inflate(R.layout.fb_text_view,flexboxLayout, false);
            TextView tv = v.findViewById(R.id.fb_text);
            String zoneId = Constants.formatInt(z.getZone()+1);
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
            tv.setOnClickListener(view -> ZoneAlert.getZoneAlert(this, null, this, z.getZone()));
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
        etPort.setText(Constants.formatInt(settings.getPort()));

        builder.setView(v);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String ip = etIp.getText().toString();
            String port = etPort.getText().toString();
            settings.setIp(ip);
            settings.setPort(Integer.parseInt(port));
            settings.toFile();
            if (listener != null)
                listener.onUpdateUrl();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private boolean setFilePath() {
        file = this.getExternalFilesDir("Settings");
        boolean result = file.exists() || file.mkdir();
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
        setup.setPrograms(programs);
        setup.toFile();
    }

    private void loadDelay(JSONObject object) {
        try {
            String action = object.getString("action");
            String status = object.getString("status");
            if (status.equals("success")) {
                if (action.equals("set")) {
                    String dt = object.getString("date");
                    Date delay = Constants.sdf.parse(dt);
                    current.getProgram().setDelay(dt);
                    Toast.makeText(this, "Delay expires at " + dt, Toast.LENGTH_SHORT).show();
                    assert delay != null;
                    setStatus("Delay until " + Constants.sdfDisplay.format(delay));
                } else if (action.equals("cancel")) {
                    Toast.makeText(this, "Delay cancelled.", Toast.LENGTH_SHORT).show();
                    cancelStatus();
                }
            } else {
                String msg = action + " Delay Failed.";
                msg = capitalizeString(msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException | ParseException e) {
            Log.e(TAG, "loadDelay() error : " + e.getMessage());
            Toast.makeText(this, "Delay not set. Request failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProgram(JSONObject object) {
        try {
            String action = object.getString("action");
            String status = object.getString("status");
            if (status.equals("success")) {
                if (action.equals("cancel")) {
                    Toast.makeText(this, "Program cancelled.", Toast.LENGTH_SHORT).show();
                    cancelStatus();
                }
            } else {
                String msg = action + " Program Failed.";
                msg = capitalizeString(msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "loadProgram() error : " + e.getMessage());
            Toast.makeText(this, "Program request failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelStatus() {
        LinearLayout llStatus = findViewById(R.id.ll_status);
        ImageButton ibStatus = findViewById(R.id.ib_status);
        llStatus.setVisibility(View.GONE);
        ibStatus.setVisibility(View.GONE);
    }

    private void setStatus(final String value) {
        LinearLayout llStatus = findViewById(R.id.ll_status);
        TextView tvStatus = findViewById(R.id.tv_status);
        ImageButton ibStatus = findViewById(R.id.ib_status);
        llStatus.setVisibility(View.VISIBLE);
        ibStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(value);
        ibStatus.setOnClickListener(view -> {
            if (value.contains("Delay until")) {
                cancelDelay();
            } else if (value.contains("running")) {
                cancelProgram();
            }
        });
    }

    public static String capitalizeString(String str) {
        String retStr = str;
        try { // We can face index out of bound exception if the string is null
            retStr = str.substring(0, 1).toUpperCase() + str.substring(1);
        }catch (Exception e){
            Log.e(TAG, "capitalizeString() error: " + e.getMessage());
        }
        return retStr;
    }

    @Override
    public void onUpdateProgram(int pPos) {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : allFragments) {
            ProgramFragment f1 = (ProgramFragment) fragment;
            if (f1.getPos() == pPos)
                f1.updateProgram();
        }
        saveProgram();
    }

    @Override
    public void onUpdateStep(int pPos) {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : allFragments) {
            ProgramFragment f1 = (ProgramFragment) fragment;
            if (f1.getPos() == pPos)
                f1.updateSteps(f1.getContext());
        }
        saveProgram();
    }

    @Override
    public void onUpdateUrl() {
        String val = settings.getIp() + ":" + Constants.formatInt(settings.getPort());
        ipText.setText(val);
    }

    @Override
    public void onCreateZone() {
        loadFlexBox();
        Gson gson = new Gson();
        UrlAsync async = new UrlAsync();
        async.execute("POST","update/zones", gson.toJson(zones));
        setup.setZones(zones);
        setup.toFile();
    }

    @Override
    public void onCreateProgram(boolean save) {
        pagerAdapter = new ProgramSwipeAdapter(this, programs.size(), this);
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
                case "delay":
                    loadDelay(response);
                    break;
                case "program":
                    loadProgram(response);
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
        mm[1] = (double)(x/10)*10;
        x = (int) mm[0] + 10;
        mm[0] = (double)(x/10)*10;
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

    private double getMonthlyAvgTemp(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int m = cal.get(Calendar.MONTH);
        if (setup != null && setup.getAverageTemps() != null)
            return setup.getAverageTemps()[m];
        else
            return 0.0d;
    }

    private Map<Date, Double> getTempAvgList() {
        Map<Date, Double> ret = new HashMap<>();
        for (History h : history) {
            ret.put(h.getDt(), h.getTAvg());
        }
        return ret;
    }
    private Double getTempAvg(Date dt, Map<Date,Double> map) {
        String date = Constants.sdfDate.format(dt);

        try {
            Date dateNT = Constants.sdfDate.parse(date);
            return map.get(dateNT);
        } catch (ParseException e) {
            Log.e(TAG, "getTempAvg() Error: " + e.getMessage());
            return 0.0d;
        }
    }

    private List<History.Temp> getTempList() {
        List<History.Temp> ret = new ArrayList<>();
        for (History h : history) {
            Log.d("getTempList()", Constants.sdf.format(h.getDt()));
            ret.addAll(h.getHistory());
        }
        ret.sort(History.Temp.comparator);
        return ret;
    }

    private int getThemeColor(int val) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(val, typedValue, true);
        return typedValue.data;
    }

    private void getAvg(List<Double> list, double val) {
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

        //return list;
    }

    private void addListValue(List<Double> list, double value, int size) {
        list.add(0, value);
        int listSize = list.size();
        while (listSize > size) {
            list.remove(listSize-1);
            listSize = list.size();
        }
    }

    private double getListAvg(List<Double> list) {
        double tot = 0.0d;
        for (Double d : list)
            tot += d;

        if (list.size() > 0)
            return tot/(double)list.size();
        return 0.0d;
    }

    private void drawHistory() {
        Log.d(TAG, "drawHistory() x["+ iHistW + "] + y[" + iHistH + "]");
        Bitmap bitmap = Bitmap.createBitmap(iHistW, iHistH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int xOff = 80;
        long minMs = getMinMs(historyDays-1);
        double[] mm = getMaxMin();
        double xMs = (double)(iHistW-xOff)/(historyDays*24*3600*1000);
        int tL = (mm[0]-mm[1]>50 ? 15 : 10);
        double yD = (double) iHistH/(5*tL);
        //double yD = (double)iHistH/(mm[0]-mm[1]);
        double yDH = (double) iHistH/100d;

        Paint pTemp = new Paint();
        pTemp.setStyle(Paint.Style.STROKE);
        pTemp.setColor(getThemeColor(R.attr.colorSecondary));
        pTemp.setStrokeWidth(getPxFromDp(1.5f));

        Paint pAvg = new Paint();
        pAvg.setStyle(Paint.Style.STROKE);
        pAvg.setColor(getThemeColor(R.attr.colorSecondary));
        pAvg.setStrokeWidth(getPxFromDp(0.5f));

        Paint pHumid = new Paint();
        pHumid.setStyle(Paint.Style.FILL);
        pHumid.setColor(getThemeColor(R.attr.colorSecondary));
        pHumid.setAlpha(60);

        Paint pLine = new Paint();
        pLine.setStyle(Paint.Style.STROKE);
        pLine.setColor(getColor(R.color.gray));
        pLine.setStrokeWidth(1);
        pLine.setTextSize(getPxFromDp(10f));

        Paint pTxt = new Paint();
        pTxt.setStyle(Paint.Style.FILL);
        pTxt.setColor(getColor(R.color.gray));
        pTxt.setStrokeWidth(1);
        pTxt.setTextSize(getPxFromDp(15f));
        pTxt.setTextAlign(Paint.Align.RIGHT);

        List<Double> tAvg = new ArrayList<>();
        List<Double> hAvg = new ArrayList<>();
        int i = (int) mm[0] - tL;

        List<Double> avg = new ArrayList<>();
        List<History.Temp> tempList = getTempList();
        int avgSize = tempList.size()/(historyDays/2);
        Map<Date, Double> tempAvgMap = getTempAvgList();

        while (i > mm[0] - 5*tL) {
            double y = (mm[0]-i)*yD;
            Path p = new Path();
            p.moveTo(xOff, (float)y);
            p.lineTo(iHistW, (float)y);
            canvas.drawPath(p, pLine);
            canvas.drawText(Constants.formatInt(i)+"\u00B0", xOff-10, (float)y+getPxFromDp(5f), pTxt);
            i -= tL;
        }

        for (int d=0; d<historyDays-1; d++) {
            double x = xOff+(getMinMs(d)-minMs)*xMs;
            Path p = new Path();
            p.moveTo((float)x, 0);
            p.lineTo((float)x, iHistH);
            canvas.drawPath(p, pLine);
        }

        Path pT = new Path();
        Path pH = new Path();
        Path pA = new Path();
        Path pMA = new Path();
        pH.moveTo((float) xOff, (float) iHistH);
        boolean move = true;
        double x = xOff;
        for (History.Temp t : tempList) {
            addListValue(avg, t.getT(), avgSize);
            double mA = getMonthlyAvgTemp(t.getTime());
            //double a = getTempF(getTempAvg(t.getTime(), tempAvgMap));
            double a = getTempF(getListAvg(avg));
            getAvg(tAvg, t.getT());
            getAvg(hAvg, t.getH());
            double tmp = tAvg.get(tAvg.size()-1);
            double hum = hAvg.get(hAvg.size()-1);
            x = xOff+(t.getTime().getTime()-minMs)*xMs;
            double fTmp = getTempF(tmp);
            double y = (mm[0]-fTmp)*yD;
            double yH = (100-hum)*yDH;
            double yA = (mm[0]-a)*yD;
            double yMA = (mm[0]-mA)*yD;
            if (x >= xOff) {
                if (move) {
                    pT.moveTo((float) x, (float) y);
                    pA.moveTo((float) x, (float) yA);
                    pMA.moveTo((float) x, (float) yMA);
                } else {
                    pT.lineTo((float) x, (float) y);
                    pA.lineTo((float) x, (float) yA);
                    pMA.lineTo((float) x, (float) yMA);
                }
                pH.lineTo((float) x, (float) yH);
                move = false;
            }
        }
        pH.lineTo((float) x, (float) iHistH);
        pH.close();

        canvas.drawPath(pH, pHumid);
        canvas.drawPath(pA, pAvg);
        pAvg.setColor(getThemeColor(R.attr.colorSecondaryVariant));
        canvas.drawPath(pMA, pAvg);
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
        //setup.setHistories(history);
        //setup.toFile();
        drawHistory();
        lastRefresh = Calendar.getInstance().getTimeInMillis();
    }

    private void loadTemp(JSONObject val) throws JSONException {
        DecimalFormat df0 = new DecimalFormat("#");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(History.Temp.dateFormat);
        Gson gson = gsonBuilder.create();
        current = gson.fromJson(val.toString(), Current.class);
        Log.d(TAG,"loadTemp() " + current.toString());

        String cur = getFTempString(current.getTemp()) + " @ ";
        int ss_l = cur.length();
        cur += "" + df0.format(current.getHumidity()) + "%";
        SpannableString ss = new SpannableString(cur);
        ss.setSpan(new RelativeSizeSpan(.85f), ss_l-5, ss_l, 0);
        ss.setSpan(new RelativeSizeSpan(.85f), ss.length()-1, ss.length(), 0);
        TextView tv = findViewById(R.id.tv_current);
        tv.setText(ss);
        if (current.getProgram().getName() != null) {
            int step_cnt = 0;
            for (Program p : programs) {
                if (p.getName().equals(current.getProgram().getName()))
                    step_cnt = p.getSteps().size();
            }
            int step_no = current.getProgram().getStep()+1;
            cur = current.getProgram().getName() + " running Step " + step_no + " of " + step_cnt + " - " +
                    current.getProgram().getTime() + " of " + current.getProgram().getRunTime()/60 + " min";
            setStatus(cur);
        } else if (!TextUtils.isEmpty(current.getProgram().getDelay())) {
            try {
                String dt = current.getProgram().getDelay();
                Date delay = Constants.sdf.parse(dt);
                current.getProgram().setDelay(dt);
                Toast.makeText(this, "Delay expires at " + dt, Toast.LENGTH_SHORT).show();
                assert delay != null;
                setStatus("Delay until " + Constants.sdfDisplay.format(delay));
            } catch (ParseException e) {
                Log.e(TAG, "loadTemp() error Delay date parse failed. " + e.getMessage());
                Toast.makeText(this, "Load Delay date failed.", Toast.LENGTH_SHORT).show();
            }
        }
            cur = "";
        tv = findViewById(R.id.tv_current_prog);
        tv.setText(cur);

        String txt = "cT: " + getTempString(val.getDouble("temp")) + "\n";
        txt += "cH: " + df0.format(val.getDouble("humidity")) + "%\n";
        txt += "aT: " + getTempString(val.getDouble("avgTemp")) + "\n";
        txt += "aH: " + df0.format(val.getDouble("avgHumidity")) + "%\n";
        txt += "\u02C4T: " + getTempString(val.getDouble("tempMax")) + "\n";
        txt += "\u02C5T: " + getTempString(val.getDouble("tempMin")) + "\n";

        if (mainText != null) {
            mainText.setText(txt);
        }
    }

    private void loadSetup(JSONObject val) throws JSONException {
        JSONObject in = val.getJSONObject("setup");
        JSONArray jPrograms = in.getJSONArray("programs");
        JSONArray jZones = in.getJSONArray("zones");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        Gson gson = gsonBuilder.create();
        setup = gson.fromJson(in.toString(), Setup.class);
        for (int i=0; i < jZones.length(); i++) {
            JSONObject z = (JSONObject) jZones.get(i);
            Zone newZ = gson.fromJson(z.toString(), Zone.class);
            if (!zones.contains(newZ))
                zones.add(newZ);
        }
        for (int i=0; i < jPrograms.length(); i++) {
            JSONObject obj = (JSONObject) jPrograms.get(i);
            Program newP = gson.fromJson(obj.toString(), Program.class);
            if (!programs.contains(newP))
                programs.add(newP);
            else {
                int i2 = programs.indexOf(newP);
                programs.set(i2, newP);
            }
        }
        loadFlexBox();
        setup.setPrograms(programs);
        setup.setZones(zones);
        setup.toFile();
        Log.d(TAG, "loadSetup() JSON: " + val.toString());
        Log.d(TAG, "loadSetup() Setup: " + setup.toString());
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

    public void setDelay(View v) {
        new StatusAlert().withContext(this)
                .withListener(this)
                .setDelay();
    }

    public void cancelDelay() {
        new StatusAlert().withContext(this)
                .withListener(this)
                .cancelDelay();
    }

    public void cancelProgram() {
        new StatusAlert().withContext(this)
                .withListener(this)
                .cancelProgram();
    }

    public static String getTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#.#");
        return df1.format(c) + "\u00B0C [" + df1.format(getTempF(c)) + "\u00B0F]";
    }

    public static String getFTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#");
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

    @Override
    public void onPause() {
        super.onPause();
    }
}
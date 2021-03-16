package com.rn5.pisprinkler.define;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.rn5.pisprinkler.ProgramAdapter;
import com.rn5.pisprinkler.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import lombok.Data;

import static com.rn5.pisprinkler.MainActivity.programs;
import static com.rn5.pisprinkler.MainActivity.zones;
import static com.rn5.pisprinkler.define.Constants.sdf;
import static com.rn5.pisprinkler.define.Constants.sdfTime;

@Data
public class ProgramAlert  {
    private static final String TAG = ProgramAlert.class.getSimpleName();

    private int hour = 0;
    private int hour24 = 0;
    private int minute = 0;
    private Date startDt;
    private Context context;
    private ProgramAdapter adapter;

    public ProgramAlert() {
        startDt = Calendar.getInstance().getTime();
    }
    public ProgramAlert withContext(Context context) {
        this.context = context;
        return this;
    }
    public ProgramAlert withAdapter(ProgramAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public static TimePickerDialog getTimePicker(Context context, TimePickerDialog.OnTimeSetListener listener, Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return new TimePickerDialog(context, listener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context));
    }

    public static AlertDialog.Builder getDeleteProgramAlert(Context context, ProgramAdapter adapter, final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.popup_zone_delete, null);
        TextView title = v.findViewById(R.id.tv_zone);
        String strZone = String.format(Locale.US,"%d",pos+1);
        Log.d(TAG, "pos[" + pos + "] " + strZone);
        title.setText(strZone);
        builder.setView(v);
        builder.setPositiveButton("OK",(dialog,which)-> {
            programs.remove(pos);
            if (adapter != null)
                adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });
        return builder;
    }

    public static AlertDialog.Builder getProgramAlert(ProgramAlert alert, final int pos) {


        AlertDialog.Builder builder = new AlertDialog.Builder(alert.getContext());
        View v = LayoutInflater.from(alert.getContext()).inflate(R.layout.popup_program, null);
        final TextView tvStartTime = v.findViewById(R.id.tv_run_time);
        final EditText etInterval = v.findViewById(R.id.et_interval);
        final EditText etName = v.findViewById(R.id.et_name);
        TimePickerDialog.OnTimeSetListener listener = (timePicker, hour, min) -> {
            alert.setHour(hour);
            alert.setMinute(min);
            try {
                alert.setStartDt(sdf.parse("2020-01-01 " + hour + ":" + min + ":00"));
            } catch (ParseException e) {
                Log.e(TAG, "Date ParseException: " + e.getMessage());
            }
            String time = (hour<10?"0"+hour:hour) + ":" + (min<10?"0"+min:min);
            tvStartTime.setText(sdfTime.format(alert.getStartDt()));
        };
        ImageButton btTime = v.findViewById(R.id.ib_run_time);
        btTime.setOnClickListener(view -> {
            getTimePicker(alert.getContext(), listener, alert.getStartDt()).show();
        });
        builder.setView(v);
        builder.setPositiveButton("OK",(dialog,which)-> {
            Date dt = Calendar.getInstance().getTime();
            try {
                dt = sdf.parse("2020-01-01 " + tvStartTime.getText().toString() + ":00");
            } catch (ParseException e) {
                Log.e(TAG, "Date ParseException: " + e.getMessage());
            }
            int interval = Integer.parseInt(etInterval.getText().toString());
            String name = etName.getText().toString();

            if (pos < programs.size()) {
                programs.get(pos).setStartTime(dt);
                programs.get(pos).setInterval(interval);
                programs.get(pos).setName(name);
            } else {
                Program program = new Program().withName(name)
                        .withStartTime(dt)
                        .withInterval(interval)
                        .setNextRunTime();
                programs.add(program);
            }
            if (alert.getAdapter() != null)
                alert.getAdapter().notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });

        return builder;
    }

    public static AlertDialog.Builder getStepAlert(ProgramAlert alert, final FlexboxLayout fb, final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(alert.getContext());
        View v = LayoutInflater.from(alert.getContext()).inflate(R.layout.popup_step, null);
        Spinner sZone = v.findViewById(R.id.step_zone_spinner);
        List<Integer> zoneIds = new ArrayList<>();
        for (Zone z : zones) {
            zoneIds.add(z.getZone());
        }
        ArrayAdapter<Integer> aZone = new ArrayAdapter<>(alert.getContext(), R.layout.spinner_item, zoneIds);
        aZone.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sZone.setAdapter(aZone);

        /*
        final TextView tvStartTime = v.findViewById(R.id.tv_run_time);
        final EditText etInterval = v.findViewById(R.id.et_interval);
        final EditText etName = v.findViewById(R.id.et_name);
        TimePickerDialog.OnTimeSetListener listener = (timePicker, hour, min) -> {
            alert.setHour(hour);
            alert.setMinute(min);
            try {
                alert.setStartDt(sdf.parse("2020-01-01 " + hour + ":" + min + ":00"));
            } catch (ParseException e) {
                Log.e(TAG, "Date ParseException: " + e.getMessage());
            }
            String time = (hour<10?"0"+hour:hour) + ":" + (min<10?"0"+min:min);
            tvStartTime.setText(sdfTime.format(alert.getStartDt()));
        };
        ImageButton btTime = v.findViewById(R.id.ib_run_time);
        btTime.setOnClickListener(view -> {
            getTimePicker(alert.getContext(), listener, alert.getStartDt()).show();
        });

         */
        builder.setView(v);
        /*
        builder.setPositiveButton("OK",(dialog,which)-> {
            Date dt = Calendar.getInstance().getTime();
            try {
                dt = sdf.parse("2020-01-01 " + tvStartTime.getText().toString() + ":00");
            } catch (ParseException e) {
                Log.e(TAG, "Date ParseException: " + e.getMessage());
            }
            int interval = Integer.parseInt(etInterval.getText().toString());
            String name = etName.getText().toString();

            if (pos < programs.size()) {
                programs.get(pos).setStartTime(dt);
                programs.get(pos).setInterval(interval);
                programs.get(pos).setName(name);
            } else {
                Program program = new Program().withName(name)
                        .withStartTime(dt)
                        .withInterval(interval)
                        .setNextRunTime();
                programs.add(program);
            }
            if (alert.getAdapter() != null)
                alert.getAdapter().notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });

         */

        return builder;
    }
}

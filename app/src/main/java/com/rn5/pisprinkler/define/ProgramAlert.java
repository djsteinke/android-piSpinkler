package com.rn5.pisprinkler.define;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.rn5.pisprinkler.adapter.ProgramAdapter;
import com.rn5.pisprinkler.R;
import com.rn5.pisprinkler.listener.CreateListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import lombok.Data;

import static com.rn5.pisprinkler.MainActivity.programs;
import static com.rn5.pisprinkler.MainActivity.zones;
import static com.rn5.pisprinkler.define.Constants.formatInt;
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
    private CreateListener listener;

    public ProgramAlert() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,6);
        cal.set(Calendar.MINUTE, 30);
        startDt = cal.getTime();
    }
    public ProgramAlert withListener(CreateListener listener) {
        this.listener = listener;
        return this;
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

    public static void getProgramAlert(ProgramAlert alert, final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(alert.getContext());
        View v = LayoutInflater.from(alert.getContext()).inflate(R.layout.popup_program, null);
        final TextView tvStartTime = v.findViewById(R.id.tv_run_time);
        final EditText etInterval = v.findViewById(R.id.et_interval);
        final EditText etName = v.findViewById(R.id.et_name);
        if (pos < programs.size()) {
            etName.setText(programs.get(pos).getName());
            etInterval.setText(formatInt(programs.get(pos).getInterval()));
            tvStartTime.setText(sdfTime.format(programs.get(pos).getStartTime()));
            alert.setStartDt(programs.get(pos).getStartTime());
        }
        etName.requestFocus();
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
                //dt = sdfTime.parse("2020-01-01 " + tvStartTime.getText().toString() + ":00");
                dt = sdfTime.parse(tvStartTime.getText().toString());
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
            if (alert.getListener() != null)
                alert.getListener().onCreateProgram();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });
        builder.setNeutralButton("Delete", ((dialog, which) -> {}));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void getDeleteStepAlert(ProgramAlert alert, final FlexboxLayout fb, final int pos, final int pPos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(alert.getContext());
        View v = LayoutInflater.from(alert.getContext()).inflate(R.layout.popup_step_delete, null);
        TextView title = v.findViewById(R.id.tv_id);
        String strZone = String.format(Locale.US,"%d",pos+1);
        Log.d(TAG, "pos[" + pos + "] " + strZone);
        title.setText(strZone);
        builder.setView(v);
        builder.setPositiveButton("OK",(dialog,which)-> {
            programs.get(pPos).removeStep(pos);
            if (alert.getAdapter() != null)
                alert.getAdapter().notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });
        builder.show();
    }

    public static void getStepAlert(ProgramAlert alert, final FlexboxLayout fb, final int pos, final int pPos) {

        final int id = pos+1;
        AlertDialog.Builder builder = new AlertDialog.Builder(alert.getContext());
        View v = LayoutInflater.from(alert.getContext()).inflate(R.layout.popup_step, null);
        Spinner sZone = v.findViewById(R.id.step_zone_spinner);
        List<Integer> zoneIds = new ArrayList<>();
        for (Zone z : zones) {
            zoneIds.add(z.getZone()+1);
        }
        Program.Step s = null;
        if (pos < programs.get(pPos).getSteps().size()) {
            s = programs.get(pPos).getSteps().get(pos);
        }
        ArrayAdapter<Integer> aZone = new ArrayAdapter<>(alert.getContext(), R.layout.spinner_item, zoneIds);
        aZone.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sZone.setAdapter(aZone);

        EditText etTime = v.findViewById(R.id.et_time);
        EditText etPercent = v.findViewById(R.id.et_percent);
        TextView tvId = v.findViewById(R.id.step_id);
        tvId.setText(String.format(Locale.US, "%d", id));

        CheckBox cbTime = v.findViewById(R.id.cb_time);
        CheckBox cbPercent = v.findViewById(R.id.cb_percent);
        cbTime.setOnCheckedChangeListener((compoundButton, b) -> {
            cbPercent.setChecked(!b);
        });
        cbPercent.setOnCheckedChangeListener((compoundButton, b) -> {
            cbTime.setChecked(!b);
        });

        if (s != null) {
            if (s.getTime() > 0) {
                etTime.setText(formatInt(s.getTime()));
                cbTime.setChecked(true);
            } else {
                etPercent.setText(formatInt(s.getPercent()));
                cbPercent.setChecked(true);
            }
        }

        builder.setView(v);
        builder.setPositiveButton("OK",(dialog,which)-> {

        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });
        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {

            int time = 0;
            int percent = 0;
            boolean bError;
            if (cbPercent.isChecked()) {
                bError = etPercent.getText().toString() == null || etPercent.getText().toString().equals("");
                if (!bError)
                    percent = Integer.parseInt(etPercent.getText().toString());
                else
                    Toast.makeText(alert.getContext(), "Percent selected. Please enter a value.", Toast.LENGTH_LONG).show();
            } else {
                bError = etTime.getText().toString() == null || etTime.getText().toString().equals("");
                if (!bError)
                    time = Integer.parseInt(etTime.getText().toString());
                else
                    Toast.makeText(alert.getContext(), "Time selected. Please enter a value.", Toast.LENGTH_LONG).show();
            }

            if (!bError) {
                if (pos >= programs.get(pPos).getSteps().size()) {
                    ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(alert.getContext()).inflate(R.layout.fb_step_view, fb, false);
                    TextView tv = cl.findViewById(R.id.fb_step);
                    tv.setText(formatInt(id));
                    TextView tvZ = cl.findViewById(R.id.fb_zone_id);
                    TextView tvT = cl.findViewById(R.id.fb_time);
                    tvZ.setText(formatInt(sZone.getSelectedItemPosition() + 1));
                    String val = formatInt((cbPercent.isChecked() ? percent : time)) + (cbPercent.isChecked() ? "%" : " MIN");
                    SpannableString ss = new SpannableString(val);
                    ss.setSpan(new RelativeSizeSpan(.75f), val.length() - (cbPercent.isChecked() ? 1 : 3), val.length(), 0); // set size
                    tvT.setText(ss);
                    cl.setOnClickListener(view1 -> ProgramAlert.getStepAlert(alert, fb, pos, pPos));
                    fb.addView(cl);
                    programs.get(pPos).addStep(pos, sZone.getSelectedItemPosition(), percent, time);
                } else {
                    for (Program.Step pS : programs.get(pPos).getSteps()) {
                        if (pS.getStep() == pos) {
                            pS.setPercent(percent);
                            pS.setTime(time);
                            pS.setZone(sZone.getSelectedItemPosition());
                            alert.getAdapter().notifyDataSetChanged();
                        }
                    }
                }
                dialog.dismiss();
            }

        });
    }
}

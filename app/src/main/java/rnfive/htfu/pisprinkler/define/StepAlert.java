package rnfive.htfu.pisprinkler.define;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import rnfive.htfu.pisprinkler.R;
import rnfive.htfu.pisprinkler.listener.CreateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import lombok.Data;

import static rnfive.htfu.pisprinkler.MainActivity.programs;
import static rnfive.htfu.pisprinkler.MainActivity.zones;
import static rnfive.htfu.pisprinkler.define.Constants.formatInt;

@Data
public class StepAlert {

    private final Context context;
    private CreateListener listener;

    public StepAlert(Context context) {
        this.context = context;
    }
    public StepAlert withListener(CreateListener listener) {
        this.listener = listener;
        return this;
    }

    public static void getStepAlert(StepAlert alert, final int sPos, final int pPos) {

        final int stepId = sPos+1;
        AlertDialog.Builder builder = new AlertDialog.Builder(alert.getContext());
        View v = LayoutInflater.from(alert.getContext()).inflate(R.layout.popup_step, null);
        Spinner sZone = v.findViewById(R.id.step_zone_spinner);
        List<Integer> zoneIds = new ArrayList<>();
        for (Zone z : zones) {
            zoneIds.add(z.getZone()+1);
        }
        Step s = new Step();
        if (sPos < programs.get(pPos).getSteps().size()) {
            s = programs.get(pPos).getSteps().get(sPos);
        }
        ArrayAdapter<Integer> aZone = new ArrayAdapter<>(alert.getContext(), R.layout.spinner_item, zoneIds);
        aZone.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sZone.setAdapter(aZone);
        sZone.setSelection(s.getZone());

        EditText etTime = v.findViewById(R.id.et_time);
        EditText etPercent = v.findViewById(R.id.et_percent);
        TextView tvId = v.findViewById(R.id.step_id);
        tvId.setText(String.format(Locale.US, "%d", stepId));
        EditText etWait = v.findViewById(R.id.et_delay);

        CheckBox cbTime = v.findViewById(R.id.cb_time);
        CheckBox cbPercent = v.findViewById(R.id.cb_percent);
        cbTime.setOnCheckedChangeListener((compoundButton, b) -> {
            cbPercent.setChecked(!b);
        });
        cbPercent.setOnCheckedChangeListener((compoundButton, b) -> {
            cbTime.setChecked(!b);
        });

        etWait.setText("0");
        if (s != null) {
            if (s.getTime() > 0) {
                etTime.setText(formatInt(s.getTime()));
                cbTime.setChecked(true);
            } else {
                etPercent.setText(formatInt(s.getPercent()));
                cbPercent.setChecked(true);
            }
            etWait.setText(formatInt(s.getWait()));
        }

        builder.setView(v);
        builder.setPositiveButton("Save",(dialog,which)-> {

        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });
        if (sPos < programs.get(pPos).getSteps().size()) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                programs.get(pPos).removeStep(sPos);
                if (alert.listener != null)
                    alert.listener.onUpdateStep(pPos);
            });
        }

        AlertDialog dialog = builder.show();

        Step finalS = s;
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

            int wait = Integer.parseInt(etWait.getText().toString());

            if (!bError) {
                if (sPos >= programs.get(pPos).getSteps().size()) {
                    finalS.setPercent(percent);
                    finalS.setTime(time);
                    finalS.setStep(sPos);
                    finalS.setZone(sZone.getSelectedItemPosition());
                    finalS.setWait(wait);
                    programs.get(pPos).addStep(finalS);
                } else {
                    for (Step pS : programs.get(pPos).getSteps()) {
                        if (pS.getStep() == sPos) {
                            pS.setPercent(percent);
                            pS.setTime(time);
                            pS.setWait(wait);
                            pS.setZone(sZone.getSelectedItemPosition());
                        }
                    }
                }
                if (alert.listener != null)
                    alert.listener.onUpdateStep(pPos);
                dialog.dismiss();
            }

        });
    }
}

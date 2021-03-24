package com.rn5.pisprinkler;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.rn5.pisprinkler.define.Program;
import com.rn5.pisprinkler.define.ProgramAlert;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import static com.rn5.pisprinkler.MainActivity.programs;
import static com.rn5.pisprinkler.define.Constants.formatInt;
import static com.rn5.pisprinkler.define.Constants.sdfDisplay;
import static com.rn5.pisprinkler.define.Constants.sdfTime;

public class ProgramFragment extends Fragment {

    private int pos;
    public ProgramFragment() {}
    public ProgramFragment withPos(int pos) {
        this.pos = pos;
        return this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View vItem = (ViewGroup) inflater.inflate(
                R.layout.fragment_program, container, false);
        //vItem.setOnClickListener(view -> ZoneAlert.getZoneAlert(this.context, this, position).show());
        //vItem.setOnLongClickListener(view -> {ZoneAlert.getDeleteZoneAlert(this.context, this, position).show(); return true;});
        final TextView name = vItem.findViewById(R.id.name);
        final TextView tv_start = vItem.findViewById(R.id.start_time);
        final TextView tv_interval = vItem.findViewById(R.id.tv_interval);
        final TextView next = vItem.findViewById(R.id.next);
        FlexboxLayout fb = vItem.findViewById(R.id.step_flex_box);

        ImageButton btAddStep = vItem.findViewById(R.id.ib_add_step);
        btAddStep.setOnClickListener(view -> {
            //ProgramAlert.getStepAlert(this.alert, fb, programs.get(pos).getSteps().size(), pos);
        });

        name.setText(programs.get(pos).getName());
        tv_start.setText(sdfTime.format(programs.get(pos).getStartTime()));
        String interval = programs.get(pos).getInterval() + " DAYS";
        tv_interval.setText(interval);
        next.setText(sdfDisplay.format(programs.get(pos).getNextRunTime()));

        fb.removeAllViews();
        for (Program.Step s : programs.get(pos).getSteps()) {
            ConstraintLayout cl = fb.findViewById(s.getStep());
            if (cl != null) {
                populateCl(cl, s);
            } else {
                cl = (ConstraintLayout) inflater.inflate(R.layout.fb_step_view, fb, false);
                cl.setId(s.getStep());

                populateCl(cl, s);

                //cl.setOnClickListener(view -> ProgramAlert.getStepAlert(alert, fb, s.getStep(), pos));
                //cl.setOnLongClickListener(view -> {ProgramAlert.getDeleteStepAlert(alert, fb, s.getStep(), pos); return true;});

                fb.addView(cl);
            }
        }
        return vItem;
    }

    private ConstraintLayout populateCl(ConstraintLayout cl, Program.Step s) {
        TextView tvZ = cl.findViewById(R.id.fb_zone_id);
        tvZ.setText(formatInt(s.getZone() + 1));

        TextView tv = cl.findViewById(R.id.fb_step);
        SpannableString ss1 = new SpannableString(formatInt(s.getStep() + 1));
        tv.setText(ss1);

        TextView tvT = cl.findViewById(R.id.fb_time);
        String val = formatInt((s.getPercent() > 0 ? s.getPercent() : s.getTime())) + (s.getPercent() > 0 ? "%" : " MIN");
        SpannableString ss = new SpannableString(val);
        ss.setSpan(new RelativeSizeSpan(.75f), val.length() - (s.getPercent() > 0 ? 1 : 3), val.length(), 0); // set size
        tvT.setText(ss);
        return cl;
    }
}

package com.rn5.pisprinkler;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.rn5.pisprinkler.define.ProgramAlert;
import com.rn5.pisprinkler.define.SlideButton;
import com.rn5.pisprinkler.define.Step;
import com.rn5.pisprinkler.define.StepAlert;
import com.rn5.pisprinkler.listener.CreateListener;

import java.util.zip.Inflater;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static com.rn5.pisprinkler.MainActivity.programs;
import static com.rn5.pisprinkler.MainActivity.zones;
import static com.rn5.pisprinkler.define.Constants.formatInt;
import static com.rn5.pisprinkler.define.Constants.sdfDisplay;
import static com.rn5.pisprinkler.define.Constants.sdfTime;

@Getter
@Setter
public class ProgramFragment extends Fragment {

    private int pos;
    private Context context;
    private FlexboxLayout fb;
    private CreateListener listener;
    private StepAlert stepAlert;
    private TextView name;
    private TextView tv_start;
    private TextView tv_interval;
    private TextView next;

    public ProgramFragment() {}
    public ProgramFragment(Context context) {
        this.context = context;
    }
    public ProgramFragment withPos(int pos) {
        this.pos = pos;
        return this;
    }
    public ProgramFragment withListener(CreateListener listener) {
        this.listener = listener;
        return this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View vItem = (ViewGroup) inflater.inflate(
                R.layout.fragment_program, container, false);

        name = vItem.findViewById(R.id.name);
        tv_start = vItem.findViewById(R.id.start_time);
        tv_interval = vItem.findViewById(R.id.tv_interval);
        next = vItem.findViewById(R.id.next);
        fb = vItem.findViewById(R.id.step_flex_box);

        ImageButton btEdit = vItem.findViewById(R.id.ib_edit_slide);
        ImageButton btAddStep = vItem.findViewById(R.id.ib_add_step);
        ImageButton btEditProg = vItem.findViewById(R.id.ib_edit_program);
        stepAlert = new StepAlert(context).withListener(this.listener);
        SlideButton sb = new SlideButton(this.context, btEdit)
                .withButton(btAddStep)
                .withButton(btEditProg);
        btEdit.setOnClickListener(view -> sb.expand());
        btEditProg.setOnClickListener(view -> {
            ProgramAlert alert = new ProgramAlert()
                    .withListener(listener)
                    .withContext(vItem.getContext());
            ProgramAlert.getProgramAlert(alert, pos);
        });
        btAddStep.setOnClickListener(view -> StepAlert.getStepAlert(stepAlert, programs.get(pos).getSteps().size(), pos));

        if (programs.size() > 0) {
            updateProgram();
            updateSteps();
        }

        return vItem;
    }

    public void updateProgram() {
        name.setText(programs.get(pos).getName());
        tv_start.setText(sdfTime.format(programs.get(pos).getStartTime()));
        String interval = programs.get(pos).getInterval() + " DAYS";
        tv_interval.setText(interval);
        next.setText(sdfDisplay.format(programs.get(pos).getNextRunTime()));
    }

    public void updateSteps() {
        fb.removeAllViews();
        for (Step s : programs.get(pos).getSteps()) {
            ConstraintLayout cl = fb.findViewById(s.getStep());
            if (cl != null) {
                populateCl(cl, s);
            } else {
                cl = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.fb_step_v, fb, false);
                cl.setId(s.getStep());

                populateClv(context, cl, s, programs.get(pos).getSteps().size());

                cl.setOnClickListener(view -> StepAlert.getStepAlert(stepAlert, s.getStep(), pos));

                fb.addView(cl);
            }
        }
    }

    private ConstraintLayout populateClv(Context context, ConstraintLayout cl, Step s, int size) {
        TextView tvZ = cl.findViewById(R.id.zone_id);
        tvZ.setText(formatInt(s.getZone() + 1));
        int type = zones.get(s.getZone()).getType();
        int bk;
        switch (type) {
            case 2:
                bk = R.drawable.head_rotor;
                break;
            case 1:
                bk = R.drawable.head_rotary;
                break;
            case 0:
            default:
                bk = R.drawable.head_fixed;
                break;
        }
        tvZ.setBackground(ContextCompat.getDrawable(context, bk));
        ImageView iv = cl.findViewById(R.id.arrow);
        if (s.getStep() == size-1)
            iv.setVisibility(View.GONE);
        else
            iv.setVisibility(View.VISIBLE);
/*
        TextView tvT = cl.findViewById(R.id.time);
        String val = formatInt((s.getPercent() > 0 ? s.getPercent() : s.getTime())) + (s.getPercent() > 0 ? "%" : " MIN");
        SpannableString ss = new SpannableString(val);
        ss.setSpan(new RelativeSizeSpan(.75f), val.length() - (s.getPercent() > 0 ? 1 : 3), val.length(), 0); // set size
        tvT.setText(ss);

 */
        return cl;
    }

    private ConstraintLayout populateCl(ConstraintLayout cl, Step s) {
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

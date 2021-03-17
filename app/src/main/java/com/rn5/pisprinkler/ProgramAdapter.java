package com.rn5.pisprinkler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.rn5.pisprinkler.define.HeadType;
import com.rn5.pisprinkler.define.Program;
import com.rn5.pisprinkler.define.ProgramAlert;

import static com.rn5.pisprinkler.MainActivity.programs;
import static com.rn5.pisprinkler.define.Constants.formatInt;
import static com.rn5.pisprinkler.define.Constants.sdf;
import static com.rn5.pisprinkler.define.Constants.sdfDisplay;
import static com.rn5.pisprinkler.define.Constants.sdfTime;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import ru.santaev.outlinespan.OutlineSpan;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.MyViewHolder> {

    private final List<Program> dataSet;
    private final Context context;
    ProgramAlert alert;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        View vItem;
        MyViewHolder(View v) {
            super(v);
            vItem = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    ProgramAdapter(List<Program> myDataset, Context context) {
        dataSet = myDataset;
        this.context = context;
        this.alert = new ProgramAlert().withContext(this.context)
                .withAdapter(this);
    }

    // Create new views (invoked by the layout manager)
    @Override
    @NonNull
    public ProgramAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_program, parent, false);
        return new ProgramAdapter.MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ProgramAdapter.MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final View vItem = holder.vItem;
        //vItem.setOnClickListener(view -> ZoneAlert.getZoneAlert(this.context, this, position).show());
        //vItem.setOnLongClickListener(view -> {ZoneAlert.getDeleteZoneAlert(this.context, this, position).show(); return true;});
        final TextView name = vItem.findViewById(R.id.name);
        final TextView desc = vItem.findViewById(R.id.desc);
        final TextView next = vItem.findViewById(R.id.next);
        FlexboxLayout fb = vItem.findViewById(R.id.step_flex_box);

        ImageButton btAddStep = vItem.findViewById(R.id.ib_add_step);
        btAddStep.setOnClickListener(view -> {
            ProgramAlert.getStepAlert(this.alert, fb, programs.get(position).getSteps().size(), position);
        });

        name.setText(dataSet.get(position).getName());
        String description = "Runs at " + sdfTime.format(dataSet.get(position).getStartTime()) + " every " + dataSet.get(position).getInterval() + " days.";
        desc.setText(description);
        String n = "Next run on " + sdfDisplay.format(dataSet.get(position).getNextRunTime()) + ".";
        next.setText(n);

        for (Program.Step s : dataSet.get(position).getSteps()) {
            ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(alert.getContext()).inflate(R.layout.fb_step_view, fb, false);

            TextView tvZ = cl.findViewById(R.id.fb_zone_id);
            tvZ.setText(formatInt(s.getZone()+1));

            TextView tv = cl.findViewById(R.id.fb_step);
            SpannableString ss1 = new SpannableString(formatInt(s.getStep()+1));
            tv.setText(ss1);

            TextView tvT = cl.findViewById(R.id.fb_time);
            String val = formatInt((s.getPercent()>0?s.getPercent():s.getTime())) + (s.getPercent()>0?"%":" MIN");
            SpannableString ss = new SpannableString(val);
            ss.setSpan(new RelativeSizeSpan(.75f), val.length()-(s.getPercent()>0?1:3),val.length(), 0); // set size
            tvT.setText(ss);

            fb.addView(cl);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
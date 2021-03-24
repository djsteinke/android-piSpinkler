package com.rn5.pisprinkler.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rn5.pisprinkler.R;
import com.rn5.pisprinkler.define.HeadType;
import com.rn5.pisprinkler.define.Zone;
import com.rn5.pisprinkler.define.ZoneAlert;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ZoneAdapter extends RecyclerView.Adapter<ZoneAdapter.MyViewHolder> {

    private final List<Zone> mDataset;
    private final Context context;

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
    public ZoneAdapter(List<Zone> myDataset, Context context) {
        mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    @NonNull
    public ZoneAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_zone, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final View vItem = holder.vItem;
        vItem.setOnClickListener(view -> ZoneAlert.getZoneAlert(this.context, this, null, position));
        vItem.setOnLongClickListener(view -> {ZoneAlert.getDeleteZoneAlert(this.context, this, null, position); return true;});
        final TextView zone = vItem.findViewById(R.id.zone_id);
        final TextView type = vItem.findViewById(R.id.zone_type);
        final TextView pin = vItem.findViewById(R.id.zone_pin);

        zone.setText(Integer.toString(mDataset.get(position).getZone()+1));
        type.setText(HeadType.getStringFromInt(mDataset.get(position).getType()));
        pin.setText(Integer.toString(mDataset.get(position).getPin()));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

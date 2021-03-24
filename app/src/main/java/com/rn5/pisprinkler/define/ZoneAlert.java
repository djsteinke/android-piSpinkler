package com.rn5.pisprinkler.define;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.rn5.pisprinkler.R;
import com.rn5.pisprinkler.adapter.ZoneAdapter;
import com.rn5.pisprinkler.listener.CreateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;

import static com.rn5.pisprinkler.MainActivity.zones;

public class ZoneAlert {
    private static final String TAG = ZoneAlert.class.getSimpleName();

    private static int[] iPins;

    public ZoneAlert() {

    }
    public static void getDeleteZoneAlert(Context context, ZoneAdapter adapter, CreateListener listener, final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.popup_zone_delete, null);
        TextView title = v.findViewById(R.id.tv_zone);
        String strZone = String.format(Locale.US,"%d",pos+1);
        Log.d(TAG, "pos[" + pos + "] " + strZone);
        title.setText(strZone);
        builder.setView(v);
        builder.setPositiveButton("OK",(dialog,which)-> {
            zones.remove(pos);
            int i = 0;
            for (Zone z : zones) {
                z.setZone(i);
                i++;
            }
            if (adapter != null)
                adapter.notifyDataSetChanged();
            if (listener != null)
                listener.onCreateZone();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });

        builder.show();
    }

    public static void getZoneAlert(Context context, ZoneAdapter adapter, CreateListener listener, final int pos) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.popup_zone, null);
        TextView title = v.findViewById(R.id.tv_zone);
        String strZone = String.format(Locale.US,"%d",pos+1);
        Log.d(TAG, "pos[" + pos + "] " + strZone);
        title.setText(strZone);
        Spinner sType = v.findViewById(R.id.type_spinner);
        Spinner sPin = v.findViewById(R.id.pin_spinner);

        ArrayAdapter<CharSequence> aType = ArrayAdapter.createFromResource(context,
                R.array.zone_types, R.layout.spinner_item);
                aType.setDropDownViewResource(R.layout.spinner_dropdown_item);
                sType.setAdapter(aType);

        List<Integer> pins = new ArrayList<>();
        iPins = context.getResources().getIntArray(R.array.pins);
        for(int i : iPins)
            pins.add(i);
        ArrayAdapter<Integer> aPin = new ArrayAdapter<>(context, R.layout.spinner_item, pins);
        aPin.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sPin.setAdapter(aPin);
        if (pos < zones.size()) {
            sType.setSelection(zones.get(pos).getType());
            sPin.setSelection(getPinPos(zones.get(pos).getPin()));
        }
        builder.setView(v);
        builder.setPositiveButton("OK",(dialog,which)-> {
            Integer iPin = (Integer) sPin.getSelectedItem();
            int iType = HeadType.getIntFromString((String) sType.getSelectedItem());
            if (pos < zones.size()) {
                zones.get(pos).setPin(iPin);
                zones.get(pos).setType(iType);
            } else {
                Zone zone = new Zone().withZone(pos)
                        .withType(iType).withPin(iPin);
                zones.add(zone);
            }
            if (adapter != null)
                adapter.notifyDataSetChanged();
            if (listener != null)
                listener.onCreateZone();
        });
        builder.setNegativeButton("Cancel",(dialog,which)-> {

        });

        builder.show();
    }

    private static int getPinPos(int val) {
        for (int i=0; i < iPins.length; i++) {
            if (iPins[i] == val)
                return i;
        }
        return 0;
    }
}

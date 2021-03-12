package com.rn5.pisprinkler;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rn5.pisprinkler.define.HeadType;
import com.rn5.pisprinkler.define.Zone;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.rn5.pisprinkler.MenuUtil.menuItemSelector;

public class ZoneActivity extends AppCompatActivity {
    private static final String TAG = ZoneActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ZoneAdapter adapter;
    private List<Zone> zones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setTheme(R.style.myRecyclerView);
        setContentView(R.layout.activity_zone);

        zones = new ArrayList<>();
        zones.add(new Zone().withType(1).withZone(1).withPin(10));
        zones.add(new Zone().withType(0).withZone(2).withPin(12));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new ZoneAdapter(zones);
        recyclerView.setAdapter(adapter);

        FloatingActionButton button = findViewById(R.id.floatingActionButton);
        button.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View v = getLayoutInflater().inflate(R.layout.popup_zone, null);
            TextView title = v.findViewById(R.id.tv_zone);
            title.setText(Integer.toString(zones.size()+1));
            Spinner sType = v.findViewById(R.id.type_spinner);
            Spinner sPin = v.findViewById(R.id.pin_spinner);
            ArrayAdapter<CharSequence> aType = ArrayAdapter.createFromResource(this,
                    R.array.zone_types, R.layout.spinner_item);
            aType.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sType.setAdapter(aType);

            List<Integer> pins = new ArrayList<>();
            for (int i : getResources().getIntArray(R.array.pins))
                pins.add(i);
            ArrayAdapter<Integer> aPin = new ArrayAdapter<>(this, R.layout.spinner_item, pins);
            aPin.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sPin.setAdapter(aPin);

            builder.setView(v);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String strType = (String) sType.getSelectedItem();
                    Integer iPin = (Integer) sPin.getSelectedItem();
                    Zone zone = new Zone().withZone(zones.size()+1)
                    .withType(HeadType.getIntFromString((String)sType.getSelectedItem())).withPin(iPin);
                    zones.add(zone);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            builder.show();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = menuItemSelector(this, item, TAG);
        return result || super.onOptionsItemSelected(item);
    }
}

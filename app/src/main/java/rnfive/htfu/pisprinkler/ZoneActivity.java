package rnfive.htfu.pisprinkler;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import rnfive.htfu.pisprinkler.adapter.ZoneAdapter;
import rnfive.htfu.pisprinkler.define.ZoneAlert;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static rnfive.htfu.pisprinkler.MenuUtil.menuItemSelector;
import static rnfive.htfu.pisprinkler.MainActivityPiSprinkler.zones;

public class ZoneActivity extends AppCompatActivity {
    private static final String TAG = ZoneActivity.class.getSimpleName();

    private ZoneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setTheme(R.style.myRecyclerView);
        setContentView(R.layout.activity_zone);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new ZoneAdapter(zones, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton button = findViewById(R.id.floatingActionButton);
        button.setOnClickListener(view -> {
            Log.d(TAG,"zone size(" + zones.size() + ")");
            ZoneAlert.getZoneAlert(this, adapter, null, zones.size());
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        Gson gson = new Gson();
        UrlAsync async = new UrlAsync();
        async.execute("POST","update/zones", gson.toJson(zones));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.url_item);
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = menuItemSelector(this, item, null, TAG);
        return result || super.onOptionsItemSelected(item);
    }
}

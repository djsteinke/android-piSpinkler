package rnfive.htfu.pisprinkler;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import rnfive.htfu.pisprinkler.R;

import rnfive.htfu.pisprinkler.adapter.ProgramAdapter;
import com.google.gson.GsonBuilder;
import rnfive.htfu.pisprinkler.define.ProgramAlert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static rnfive.htfu.pisprinkler.MainActivity.programs;
import static rnfive.htfu.pisprinkler.MenuUtil.menuItemSelector;

public class ProgramActivity extends AppCompatActivity {
    private static final String TAG = ProgramActivity.class.getSimpleName();

    private ProgramAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate()");
        setTheme(R.style.myRecyclerView);
        setContentView(R.layout.activity_program);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new ProgramAdapter(programs, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton button = findViewById(R.id.floatingActionButton);
        button.setOnClickListener(view -> {
            //ProgramAlert.getProgramAlert(this, adapter, programs.size()).show();
            ProgramAlert alert = new ProgramAlert().withAdapter(adapter)
                    .withContext(this);
            ProgramAlert.getProgramAlert(alert, programs.size());
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss");
        Gson gson = gsonBuilder.create();
        UrlAsync async = new UrlAsync();
        async.execute("POST","update/programs", gson.toJson(programs));
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

package com.rn5.pisprinkler;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.rn5.pisprinkler.define.ProgramAlert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.rn5.pisprinkler.MainActivity.programs;
import static com.rn5.pisprinkler.MenuUtil.menuItemSelector;

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
            ProgramAlert.getProgramAlert(alert, programs.size()).show();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Gson gson = new Gson();
        UrlAsync async = new UrlAsync();
        async.execute("POST","update/programs", gson.toJson(programs));
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

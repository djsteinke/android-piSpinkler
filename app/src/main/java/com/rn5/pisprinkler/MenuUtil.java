package com.rn5.pisprinkler;

import android.content.Intent;
import android.view.MenuItem;

import com.rn5.pisprinkler.define.ProgramAlert;
import com.rn5.pisprinkler.listener.CreateListener;

import androidx.appcompat.app.AppCompatActivity;

import static com.rn5.pisprinkler.MainActivity.alert;

public abstract class MenuUtil extends AppCompatActivity {

    public static boolean menuItemSelector(final AppCompatActivity context, MenuItem item, CreateListener listener, String sourceActivity) {

        switch (item.getItemId()) {
            case R.id.summary_item:
                //context.startActivity(new Intent(context, MainActivity.class));
                break;
            case R.id.program_item:
                context.startActivity(new Intent(context, ProgramActivity.class));
                break;
            case R.id.zone_item:
                context.startActivity(new Intent(context, ZoneActivity.class));
                break;
            case R.id.url_item:
                alert(context, listener);
                break;
            default:
                return true;
        }

        if (item.getItemId() != R.id.d_menu_item) {
            boolean b_finish = !sourceActivity.equals("MainActivity");

            if (b_finish)
                context.finish();
        }

        return true;
    }
}


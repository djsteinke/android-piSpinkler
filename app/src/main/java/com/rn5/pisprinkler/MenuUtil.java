package com.rn5.pisprinkler;

import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public abstract class MenuUtil extends AppCompatActivity {

    public static boolean menuItemSelector(final AppCompatActivity context, MenuItem item, String sourceActivity) {

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


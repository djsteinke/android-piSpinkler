package com.rn5.pisprinkler.define;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static com.rn5.pisprinkler.MainActivity.file;
import static com.rn5.pisprinkler.define.Constants.load;
import static com.rn5.pisprinkler.define.Constants.save;

@Getter
@Setter
public class Setup {
    private static final String TAG = Setup.class.getSimpleName();
    private static final String fileName = "setup.json";

    private Long id;
    private List<Program> programs = new ArrayList<>();
    private List<Zone> zones = new ArrayList<>();
    //private List<History> histories = new ArrayList<>();

    public Setup() {}

    public static Setup fromFile() {
        try {
            Log.d(TAG, "fromFile()");
            return load(new File(file, fileName), Setup.class);
        } catch (IOException e) {
            Log.d(TAG, "fromFile() error " + e.getMessage());
            Setup setup = new Setup();
            setup.setId(Calendar.getInstance().getTimeInMillis());
            setup.toFile();
            return new Setup();
        }
    }

    public void toFile() {
        try {
            this.id = Calendar.getInstance().getTimeInMillis();
            save(new File(file, fileName), this);
        } catch (IOException e) {
            Log.e(TAG, "toFile() failed. " + e.getMessage());
        }
    }

}

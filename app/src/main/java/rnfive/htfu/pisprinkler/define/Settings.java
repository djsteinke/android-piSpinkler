package rnfive.htfu.pisprinkler.define;

import android.util.Log;

import com.google.gson.*;
import rnfive.htfu.pisprinkler.MainActivity;

import java.io.File;
import java.io.IOException;

import lombok.Data;

import static rnfive.htfu.pisprinkler.define.Constants.load;
import static rnfive.htfu.pisprinkler.define.Constants.save;

@Data
public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private int port;
    private String ip;
    private static final String fileName = "settings.json";
    private static final File file = new File(MainActivity.file, fileName);

    Settings(){
        Log.d(TAG,"Settings()");
    }

    Settings(String ip, int port) {
        Log.d(TAG,"Settings(ip[" + ip + "], port[" + port + "])");
        this.ip = ip;
        this.port = port;
    }

    public void toFile() {
        Gson gson = new GsonBuilder().create();
        String val = gson.toJson(this);
        try {
            save(file, this);
        } catch (IOException e) {
            Log.e(TAG, "toFile() error: " + e.getMessage());
        }

    }

    public static Settings fromFile() {
        Log.d(TAG,"load()");
        try {
            return load(file, Settings.class);
        } catch (IOException e) {
            Log.d(TAG, "fromFile() file does not exist.  Create new Settings().");
            Settings settings = new Settings(MainActivity.ip, MainActivity.port);
            settings.toFile();
            return settings;
        }
    }

}

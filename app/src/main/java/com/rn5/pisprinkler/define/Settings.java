package com.rn5.pisprinkler.define;

import android.util.Log;

import com.google.gson.*;
import com.rn5.pisprinkler.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import lombok.Data;

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

    public void save() {
        Gson gson = new GsonBuilder().create();
        String val = gson.toJson(this);
        try {
            if (file.exists() || (!file.exists() && file.createNewFile())) {
                if (file.canWrite() || file.setWritable(true)) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                    bw.write(val);
                    bw.close();
                }
            }
        } catch (IOException e) {
            System.out.println("WriteException : " + e.getMessage());
        }

    }

    public static Settings load() {
        Log.d(TAG,"load()");
        String inputLine;
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(sb.toString(), Settings.class);
        } catch (IOException e) {
            Settings settings = new Settings(MainActivity.ip, MainActivity.port);
            settings.save();
            return settings;
        }
    }

}

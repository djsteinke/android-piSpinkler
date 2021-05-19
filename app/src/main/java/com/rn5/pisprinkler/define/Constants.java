package com.rn5.pisprinkler.define;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rn5.pisprinkler.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import lombok.Data;

@Data
public class Constants {


    public static final SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm aa", Locale.US);
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final SimpleDateFormat sdfDisplay = new SimpleDateFormat("EEE, MMM d", Locale.US);


    public Constants() {}

    public static String formatInt(int val) {
        return String.format(Locale.US,"%d",val);
    }

    public static <T> void save(File file, T in) throws IOException{
        Gson gson = new GsonBuilder().create();
        String val = gson.toJson(in);
        if (file.exists() || (!file.exists() && file.createNewFile())) {
            if (file.canWrite() || file.setWritable(true)) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(val);
                bw.close();
            }
        }

    }

    public static <T> T load(File file, Class<T> in) throws IOException {
        String inputLine;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(sb.toString(), in);
    }
}

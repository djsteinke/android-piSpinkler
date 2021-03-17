package com.rn5.pisprinkler.define;

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
}

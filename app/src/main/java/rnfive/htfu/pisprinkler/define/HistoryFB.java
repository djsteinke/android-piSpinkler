package rnfive.htfu.pisprinkler.define;

import android.util.Log;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class HistoryFB implements Comparable<HistoryFB>{
    @Exclude
    private static final String TAG = HistoryFB.class.getSimpleName();
    public static final String dateFormat = "yyyy-MM-dd";
    private Long dt;
    private Double tAvg;
    private Double hAvg;
    private Double tMax;
    private Double tMin;
    private final Map<String, Temp> history = new HashMap<>();

    public HistoryFB() {}

    public void setDt(String stringDate) {
        try {
            Date date = new SimpleDateFormat(dateFormat, Locale.US).parse(stringDate);
            assert date != null;
            dt = date.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Date failed to parse.");
        }
    }

    @Override
    public String toString() {
        return "{dt: " + dt + ", tAvg: " + tAvg + ", hAvg: " + hAvg + ", tMax: " + tMax + ", tMin: " + tMin + "}";
    }

    @Override
    public int compareTo(HistoryFB cT) {
        long dt =cT.getDt();
        return (int) (this.getDt()-dt);
    }

    @Getter
    @Setter
    @ToString
    public static class Temp implements Comparable<Temp>{
        public static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
        private Long time;
        private Double t;
        private Double h;

        public Temp() {}

        public void setTime(String stringDate) {
            try {
                Date dt = new SimpleDateFormat(dateFormat, Locale.US).parse(stringDate);
                assert dt != null;
                time = dt.getTime();
            } catch (ParseException e) {
                Log.e(TAG, "Date failed to parse.");
            }
        }

        @Override
        public int compareTo(Temp cT) {
            long dt =cT.getTime();
            return (int) (this.time-dt);
        }
    }
}

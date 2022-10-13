package rnfive.htfu.pisprinkler.define;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class History {

    public static final String dateFormat = "yyyy-MM-dd";
    private Date dt;
    private Double tAvg;
    private Double hAvg;
    private Double tMax;
    private Double tMin;
    private List<Temp> history = new ArrayList<>();

    public History() {}

    @Getter
    @Setter
    public static class Temp {
        public static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
        private Date time;
        private Double t;
        private Double h;

        public Temp() {}

        public int compareTo(Temp cT) {
            long dt =cT.getTime().getTime();
            return (int) (this.time.getTime()-dt);
        }

        @Override
        @NonNull
        public String toString() {
            return "Temp[" + Constants.sdf.format(time) + "] T:" + t + " H:" + h;
        }

        public static Comparator<History.Temp> comparator = Temp::compareTo;
    }
}

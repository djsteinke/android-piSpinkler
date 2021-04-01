package com.rn5.pisprinkler.define;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class History {

    public static final String dateFormat = "yyyy-MM-dd";
    private Date dt;
    private Double tAvg;
    private Double hAvg;
    private Double tMax;
    private Double tMin;
    private List<Temp> history;

    public History() {}

    @Data
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

        public static Comparator<History.Temp> comparator = Temp::compareTo;
    }
}

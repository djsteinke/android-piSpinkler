package rnfive.htfu.pisprinkler.define;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Current {
    private double temp;
    private double humidity;
    private double avgTemp;
    private double avgHumidity;
    private double tempMax;
    private double tempMin;
    private Program program;

    public Current() {}

    @Getter
    @Setter
    @ToString
    public static class Program {
        private String name;
        private int step;
        private int time;
        private int runTime;
        private String delay;

        public Program(){}
    }
}

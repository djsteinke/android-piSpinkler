package com.rn5.pisprinkler.define;

import lombok.Data;

@Data
public class Step {
    private int step;
    private int zone;
    private int percent;
    private int time;

    public Step() {}
    Step(int step, int zone, int percent, int time) {
        this.step = step;
        this.zone = zone;
        this.percent = percent;
        this.time = time;
    }
}

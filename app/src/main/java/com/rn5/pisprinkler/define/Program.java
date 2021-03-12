package com.rn5.pisprinkler.define;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Program {
    private static final String TAG = Program.class.getSimpleName();
    private Date nextRunTime;
    private Date startTime;
    private int interval;
    private List<Step> steps;

    public Program() {}
    public Program withNextRunTime(Date date) {
        this.nextRunTime = date;
        return this;
    }
    public Program withStartTime(Date date) {
        this.startTime = date;
        return this;
    }
    public Program withInterval(int val) {
        this.interval = val;
        return this;
    }

    public void addStep(int step, int zone, int percent) {
        this.steps.add(new Step(step, zone, percent));
    }

    public void removeStep(int step) {
        for (Step s : this.steps) {
            if (s.getStep() == step) {
                this.steps.remove(s);
                break;
            }
        }
    }


    @Data
    public class Step {
        private int step;
        private int zone;
        private int percent;

        Step() {}
        Step(int step, int zone, int percent) {
            this.step = step;
            this.zone = zone;
            this.percent = percent;
        }
    }
}

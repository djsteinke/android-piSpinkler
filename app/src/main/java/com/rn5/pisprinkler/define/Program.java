package com.rn5.pisprinkler.define;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Program {
    private static final String TAG = Program.class.getSimpleName();
    private String name;
    private Date nextRunTime;
    private Date startTime;
    private int interval;
    private List<Step> steps = new ArrayList<>();

    public Program() {}
    public Program withName(String name) {
        this.name = name;
        return this;
    }
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
    public Program setNextRunTime() {
        Calendar stCal = Calendar.getInstance();
        stCal.setTime(startTime);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, stCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, stCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DATE, this.interval);
        this.nextRunTime = cal.getTime();
        return this;
    }

    public void addStep(int step, int zone, int percent, int time) {
        this.steps.add(new Step(step, zone, percent, time));
    }

    public void removeStep(int step) {
        for (Step s : this.steps) {
            if (s.getStep() > step) {
                int stepId = s.getStep() - 1;
                s.setStep(stepId);
            }
        }
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
        private int time;

        Step() {}
        Step(int step, int zone, int percent, int time) {
            this.step = step;
            this.zone = zone;
            this.percent = percent;
            this.time = time;
        }
    }
}

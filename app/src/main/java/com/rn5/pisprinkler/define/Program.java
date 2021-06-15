package com.rn5.pisprinkler.define;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Program {
    private static final String TAG = Program.class.getSimpleName();
    private String name;
    private Date nextRunTime;
    private Date startTime;
    private int interval;
    private boolean active;
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
    public Program isActive(boolean val) {
        this.active = val;
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

    public void addStep(int step, int zone, int percent, int time, int wait) {
        this.steps.add(new Step(step, zone, percent, time, wait));
    }

    public void addStep(Step step) {
        this.steps.add(step);
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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Program))
            return false;
        Program other = (Program)o;
        return this.name.equals(other.name);
    }

}

package rnfive.htfu.pisprinkler.define;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProgramFB {
    private String name;
    private Long nextRunTime;
    private Long startTime;
    private int interval;
    private boolean active;
    private final List<Step> steps = new ArrayList<>();

    public ProgramFB() {}
}

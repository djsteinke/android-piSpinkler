package rnfive.htfu.pisprinkler.define;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SetupFB {
    private static final String TAG = SetupFB.class.getSimpleName();

    private Double[] averageTemps;
    private Integer[][] wateringTimes;
    private Long delay = 0L;
    private final Map<String, ProgramFB> programs = new HashMap<>();
    private final List<Zone> zones = new ArrayList<>();

    public SetupFB() {}
}

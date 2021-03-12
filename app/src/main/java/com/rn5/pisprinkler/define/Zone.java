package com.rn5.pisprinkler.define;

import lombok.Data;

@Data
public class Zone {
    private static final String TAG = Zone.class.getSimpleName();
    private int zone;
    private int type;
    private int pin;

    public Zone() {}
    public Zone withZone(int zone) {this.zone = zone; return this;}
    public Zone withType(int type) {this.type = type; return this;}
    public Zone withPin(int pin) {this.pin = pin; return this;}
}

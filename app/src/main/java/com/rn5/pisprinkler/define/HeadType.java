package com.rn5.pisprinkler.define;

public enum HeadType {
    FIXED("FIXED", 0),
    ROTOR("ROTOR", 2),
    ROTARY_HE("ROTARY HE", 1),
    DEFAULT("Default", -1);

    private final String stringValue;
    private final int intValue;

    HeadType(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    public int toInt() {
        return intValue;
    }

    public String toString() {
        return stringValue;
    }

    public static HeadType fromString(String val) {
        return HeadType.valueOf(val);
    }

    public static HeadType fromInt(int val) {
        return HeadType.values()[val];
    }

    public static int getIntFromString(String val) {
        for (HeadType ht : HeadType.values()) {
            if (ht.stringValue.equals(val)) {
                return ht.intValue;
            }
        }
        return -1;
    }

    public static String getStringFromInt(int val) {
        for (HeadType ht : HeadType.values()) {
            if (ht.intValue == val) {
                return ht.toString();
            }
        }
        return "Default";
    }
}

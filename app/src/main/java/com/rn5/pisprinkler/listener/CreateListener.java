package com.rn5.pisprinkler.listener;

public interface CreateListener {
    void onCreateZone();
    void onCreateProgram();
    void onUpdateUrl();
    void onUpdateStep(int pPos);
    void onUpdateProgram(int pPos);
}

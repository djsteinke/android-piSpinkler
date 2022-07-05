package rnfive.htfu.pisprinkler.listener;

public interface CreateListener {
    void onCreateZone();
    void onCreateProgram(boolean save);
    void onUpdateUrl();
    void onUpdateStep(int pPos);
    void onUpdateProgram(int pPos);
}

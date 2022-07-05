package rnfive.htfu.pisprinkler;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}

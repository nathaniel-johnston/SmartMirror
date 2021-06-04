package com.example.smartmirror;

import android.app.Application;
import android.content.Context;

public class SmartMirror extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        SmartMirror.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SmartMirror.context;
    }
}

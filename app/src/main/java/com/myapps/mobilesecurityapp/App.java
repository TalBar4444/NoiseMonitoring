package com.myapps.mobilesecurityapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.myapps.mobilesecurityapp.activities.MainActivity;
import com.myapps.mobilesecurityapp.activities.StartActivity;
import com.myapps.mobilesecurityapp.model.MSPV;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MSPV.init(this);

    }

}
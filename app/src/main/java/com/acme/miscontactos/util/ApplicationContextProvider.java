package com.acme.miscontactos.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by alejandro on 6/10/14.
 */
public class ApplicationContextProvider extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}

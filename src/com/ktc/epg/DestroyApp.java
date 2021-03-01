package com.ktc.epg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DestroyApp extends Application implements TvSingletons {
    private static final String TAG = "DestroyApp";

    private static List<Activity> mainActivities = new ArrayList<Activity>();

    private static boolean isAppAlive = false;
    public static boolean isInWizard = false;
    private static Activity mRunningActivity = null;

    public static Context appContext;

    public static Activity getTopActivity() {
        return mRunningActivity;
    }

    public static void setRunningActivity(Activity activity) {
        mRunningActivity = activity;
    }

    public static String getRunningActivity() {
        if (mRunningActivity == null) {
            return "";
        } else {
            //android.util.Log.d(TAG, "getRunningActivity: " + mRunningActivity.getClass().getSimpleName());
            return mRunningActivity.getClass().getSimpleName();
        }
    }

    public static boolean isCurTaskTKUI() {
        return isAppAlive;
    }


    public void add(Activity act) {
        mainActivities.add(act);
    }

    public void remove(Activity activity) {
        if (mainActivities.size() > 0) {
            mainActivities.remove(activity);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        Log.d(TAG, "onCreate DestroyApp " + appContext);

    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public static TvSingletons getSingletons() {
        return (TvSingletons) appContext;
    }
}

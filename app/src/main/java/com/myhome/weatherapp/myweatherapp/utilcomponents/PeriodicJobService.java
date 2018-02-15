package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by emuanir on 2/3/2018.
 */

public class PeriodicJobService extends JobService {

    final static String TAG="DEBUG(JobService):";

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(TAG,"onStartJob(): Starting ServiceRefreshWeatherData to run as IntentService");
        startService(new Intent(this, ServiceRefreshWeatherData.class));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}

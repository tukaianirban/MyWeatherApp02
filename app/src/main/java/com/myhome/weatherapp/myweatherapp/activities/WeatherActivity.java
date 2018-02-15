package com.myhome.weatherapp.myweatherapp.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.myhome.weatherapp.myweatherapp.R;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;
import com.myhome.weatherapp.myweatherapp.utilcomponents.PeriodicJobService;
import com.myhome.weatherapp.myweatherapp.utilcomponents.ServiceRefreshWeatherData;
import com.myhome.weatherapp.myweatherapp.utilcomponents.WeatherPagerAdapter;
import com.myhome.weatherapp.myweatherapp.utils.conversionUtils;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class WeatherActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener{

    static final String TAG = "DEBUG(WeatherAct):";

    // member variables
    static final int LOADER_WEATHER_PAGER = 1025;
    static final int WEATHER_REFRESH_SERVICE_JOB_ID = 2048;

    //member views
    WeatherPagerAdapter mWeatherAdapter;
    ViewPager viewPagerCityWeather;
    TextView tvMessageWhenNoCities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        tvMessageWhenNoCities = (TextView) findViewById(R.id.tvMessageWhenNoCities);
        mWeatherAdapter = new WeatherPagerAdapter(getSupportFragmentManager(), this);
        viewPagerCityWeather = (ViewPager) findViewById(R.id.viewPagerCityWeather);
        viewPagerCityWeather.setAdapter(mWeatherAdapter);

        setMainDisplayItemsVisibility();

        getSupportLoaderManager().initLoader(LOADER_WEATHER_PAGER, null, this);

        // check if a job is already scheduled, else schedule a job to refresh the weather every 30mins
        schedulePeriodicWeatherRefresh(this, 300, null);

        setupInitialPreferences();
    }

    private void setupInitialPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // get and print the last weather update time
        String weatherRefreshTime = sharedPreferences.getString(getResources().getString(R.string.pref_last_weather_refresh_timestamp), null);
        if (weatherRefreshTime!=null)
            Log.d(TAG,"onCreate(): Weather information was last updated at "+ conversionUtils.getReadableTimeFromStamp(weatherRefreshTime));
        else{
            Log.d(TAG,"onCreate(): Weather information preference value is found to be NULL. Init it");
            SharedPreferences.Editor sharePreferenceEditor = sharedPreferences.edit();
            sharePreferenceEditor.putString(
                    getResources().getString(R.string.pref_units_key),
                    getResources().getString(R.string.pref_units_value_metric));
            sharePreferenceEditor.commit();
        }
        int constraint=0;
        int executionWindow = 0;

        // setup a listener for changes to metered network preference
        boolean refreshOnMeteredNetwork = sharedPreferences.getBoolean(
                getResources().getString(R.string.pref_metered_net_check_key),
                false);
        if (refreshOnMeteredNetwork){
            constraint = constraint | Constraint.ON_UNMETERED_NETWORK;
        }

        // setup a listener for changes to refresh interval time
        executionWindow = sharedPreferences.getInt(
                getResources().getString(R.string.pref_refresh_interval_key),
                0);

        // setup the scheduler job with the metered network check, and execution window.
        schedulePeriodicWeatherRefresh(WeatherActivity.this,
                executionWindow,
                constraint);

    }

    private void setMainDisplayItemsVisibility(){
        if (mWeatherAdapter.getCount()<=0) {
            tvMessageWhenNoCities.setVisibility(View.VISIBLE);
            viewPagerCityWeather.setVisibility(View.GONE);
        }
        else {
            tvMessageWhenNoCities.setVisibility(View.GONE);
            viewPagerCityWeather.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG,"onCreateOptionsMenu(): called");
        getMenuInflater().inflate(R.menu.actionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG,"onOptionsItemSelected(): called");
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.menuSettings:
                Log.d(TAG,"onOptionsItemSelected(): displaying Settings screen");
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menuRefresh:
                Log.d(TAG,"onOptionsItemSelected(): calling for refresh of Weather data");
                startService(new Intent(this, ServiceRefreshWeatherData.class));
                return true;
            case R.id.menuShowCities:
                Log.d(TAG,"onOptionsItemSelected(): calling for display of selected cities list");
                startActivity(new Intent(this, AddCityActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Create and schedule a job that is:
     * - extended from the Firebase.JobService
     * - is recurring in nature
     * - is triggered at some time in a 30min. window(by default)
     * - has a set of constraints as are passed in to the method call
     * - will replace any existing job with the same tag
     * - that persists forever as long as the application is installed
     * - has a tag
     */
    public static void schedulePeriodicWeatherRefresh(@NonNull Context context, @NonNull int executionWindow, int... constraints){
        Log.d(TAG,"schedulePeriodicWeatherRefresh(): Job Scheduler called with exec.Window=" + executionWindow);
        if (constraints==null || constraints.length==0)
            Log.d(TAG,"schedulePeriodicWeatherRefresh(): Null or no constraints passed in");

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        if (executionWindow<=0) executionWindow=1800;

        Job.Builder jobBuilder = jobDispatcher.newJobBuilder()
                .setService(PeriodicJobService.class)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(0,executionWindow))
                .setReplaceCurrent(true)
                .setLifetime(Lifetime.FOREVER)
                .setTag("WEATHER_REFRESHER");

        if(constraints!=null && constraints.length>0) jobBuilder.setConstraints(constraints);

        jobDispatcher.mustSchedule(jobBuilder.build());
        Log.d(TAG,"schedulePeriodicWeatherRefresh(): Firebase.JobService scheduled for refreshing the weather");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preferenceKey) {
        // if there is a change in the units' type (Imperial/Metric) by the user, then update the weather data
        // displayed to display all the weather in the User's preferred unit type.
        if (preferenceKey.equals(getResources().getString(R.string.pref_units_key))){
            mWeatherAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG,"onCreateLoader() called for loading City-info");
        return new CursorLoader(this, DBContract.CITY_CONTENT_URI, null, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG,"onLoadFinished(): called on finish loading of city-info");
        mWeatherAdapter.swapCursor(data);

        // on change of the number of cities in the mWeatherAdapter(user-selected Cities list),
        // adjust visibility of viewPager and Message Box
        setMainDisplayItemsVisibility();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWeatherAdapter.swapCursor(null);
        setMainDisplayItemsVisibility();
    }
}

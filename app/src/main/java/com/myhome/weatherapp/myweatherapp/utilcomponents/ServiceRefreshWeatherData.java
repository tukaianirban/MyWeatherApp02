package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.myhome.weatherapp.myweatherapp.R;
import com.myhome.weatherapp.myweatherapp.openweathermap.OpenWeatherUtils;
import com.myhome.weatherapp.myweatherapp.openweathermap.weatherContract;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;
import com.myhome.weatherapp.myweatherapp.utils.JSONUtils;
import com.myhome.weatherapp.myweatherapp.utils.NetworkUtils;
import com.myhome.weatherapp.myweatherapp.utils.conversionUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * A Firebase.JobService is used to execute the scheduled task in the background
 * at periodic intervals.
 */

public class ServiceRefreshWeatherData extends IntentService {

    final static String TAG = "DEBUG(SrvcRefresh):";

    public ServiceRefreshWeatherData(){
        super("ServiceRefreshWeatherData");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG,"onHandleIntent(): started IntentService to refresh the weather data");

        /* optionally the intent could contain a bundle including a cityid
         */
        Bundle bundleWIthCityId = intent.getExtras();
        String cityIdToLoad=null;
        if (bundleWIthCityId!=null)
            cityIdToLoad = bundleWIthCityId.getString(DBContract.CityList.ID, null);
        Log.d(TAG,"onHandleIntent(): invoker Intent contains cityId = "+cityIdToLoad);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String prefValueUnits = sharedPreferences.getString(getResources().getString(R.string.pref_units_key), null);
        if (prefValueUnits==null){
            Log.d(TAG,"onHandleIntent(): Units preference is null! Abort weather data refresh");
            return;
        }
        Log.d(TAG,"onHandleIntent(): Preference unit="+prefValueUnits);

        /* If particular cityIdToLoad is passed into the invoker Intent,
           get a Cursor with City information only about this city
           Else, get a Cursor with City information for all the cities in the database.
         */
        Cursor citiesCursor;
        if (cityIdToLoad!=null)
            citiesCursor = getContentResolver().query(DBContract.CITY_CONTENT_URI,
                    null,
                    DBContract.CityList.ID+"=?",
                    new String[]{cityIdToLoad},
                    null,
                    null);
        else
            citiesCursor = getContentResolver().query(DBContract.CITY_CONTENT_URI,
                    null,
                    null,
                    null,
                    null,
                    null);
        if (citiesCursor==null || citiesCursor.getCount()<1) return;

        /* iterate through the Cursor of CityList and,
           for city (listed in the local database), perform a query to server for the weather,
           convert the received JSON data into List<ContentValues>,
           delete existing Weather+Forecast data for this city from the local db
           bulkInsert the array of ContentValues into the database.
         */
        while (citiesCursor.moveToNext()){
            String cityid = citiesCursor.getString(citiesCursor.getColumnIndex(DBContract.CityList.ID));
            String cityname = citiesCursor.getString(citiesCursor.getColumnIndex(DBContract.CityList.CITY_NAME));
            Log.d(TAG,"onHandleIntent(): Query for cityname="+ cityname + " cityid="+cityid);


            List<ContentValues> weatherContentValuesList = new ArrayList<ContentValues>();

            // get the query response for "Weather", convert the data into a ContentValue and add it to the List<>
            ContentValues currentWeather = getWeatherContentFromURL(cityid, prefValueUnits);
            weatherContentValuesList.add(currentWeather);
            //broadcast toady's weather out to the widgets on home screen
            Intent intentUpdateWidgets = new Intent("android.myweatherappwidget.action.APPWIDGET_UPDATE");
            Bundle bundle = conversionUtils.getBundleFromContentValues(currentWeather);
            bundle.putString(DBContract.CityList.CITY_NAME, cityname);
            intentUpdateWidgets.putExtras(bundle);
            //startActivity(intentUpdateWidgets);
            Log.d(TAG,"onHandleIntent(): fired Intent to update the widget");

            // get the query response for "Forecast", convert the data into a List<ContentValue> and append it to the current List<>
            weatherContentValuesList.addAll(getForecastContentFromURL(cityid, prefValueUnits));


            /*
            *  add in the complete List<> of weather+forecast data into the database for this cityid
            */
            if (weatherContentValuesList.size()>0){
                Log.d(TAG,"onHandleIntent(): weather+forecast data for cityid="+cityid+" returned "+ weatherContentValuesList.size()+ " rows");

                // first delete all entries for this city-id
                Uri deleteWeatherForCityUri = DBContract.WEATHER_CONTENT_URI.buildUpon()
                        .appendPath(cityid)
                        .build();
                int rowCount = getContentResolver().delete(deleteWeatherForCityUri, null, null);
                Log.d(TAG,"onHandleIntent(): deleted "+ rowCount + " rows of weather data for city-id="+cityid+" before inserting new data");

                ContentValues[] weatherEntriesArray = new ContentValues[weatherContentValuesList.size()];
                for (int i=0;i<weatherContentValuesList.size();i++) {
                    ContentValues values = weatherContentValuesList.get(i);
                    // insert the city-id into each weatherEntry before inserting into the database
                    values.put(DBContract.WeatherEntry.CITY_ID, cityid);
                    weatherEntriesArray[i] = values;
                }
                Log.d(TAG,"onHandleIntent(): Prepared "+weatherEntriesArray.length+" rows of weather info for cityid="+cityid);
                int rowsCount = getContentResolver().bulkInsert(DBContract.WEATHER_CONTENT_URI, weatherEntriesArray);
                Log.d(TAG,"onHandleIntent(): inserted "+ rowsCount + " of weather records into database for cityid="+cityid);
                if (rowsCount>0){
                    // Send notification that "Weather+Forecast" content has changed for this 'CityId"
                    // All clients/observers of weather information should go for a refresh now
                    getContentResolver().notifyChange(
                            DBContract.WEATHER_CONTENT_URI.buildUpon().appendPath(cityid).build(),
                            null);
                }

            }else
                Log.d(TAG,"onHandleIntent(): Query to OpenWeatherApi returned no weather records for city-id="+cityid);
        }

        // update the last-weather-refresh time
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getResources().getString(R.string.pref_last_weather_refresh_timestamp), String.valueOf(System.currentTimeMillis()));
        editor.commit();

        return;       // Is there work still ongoing ?
    }

    /* query openweather for this city's "weather" data
     * Add this weather data into a ContentValues List<>
     */
    private static ContentValues getWeatherContentFromURL(String cityid, String prefValueUnits){

        ContentValues weatherCVList = new ContentValues();

        Uri weatherQueryUri = OpenWeatherUtils.getBaseWeatherUri();
        weatherQueryUri = OpenWeatherUtils.getUriWithLocationId(weatherQueryUri, cityid);
        weatherQueryUri = OpenWeatherUtils.getUriWithResultType(weatherQueryUri, "accurate");
        try {
            URL url = new URL(weatherQueryUri.toString());
            Log.d(TAG,"getWeatherContentFromURL(): URI for querying weather data for this city : "+ weatherQueryUri.toString());

            JsonReader reader = NetworkUtils.getResponseFromURL(url);

            // get the array of ContentValues from the response data from OpenWeatherApi
            weatherCVList = JSONUtils.readWeatherResponseJSON(reader);
            Log.d(TAG,"getWeatherContentFromURL(): received "+weatherCVList.size()+" weather info parsed from JSONResponse's Reader, for city-id="+cityid);
            Log.d(TAG,"getWeatherContentFromURL(): Content dump="+weatherCVList.toString());
        }catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.w(TAG, "getWeatherContentFromURL(): MalformedURLException creating URL to query. Trace:" + ex.getMessage());
        }catch(IOException ex){
            ex.printStackTrace();
            Log.w(TAG,"getWeatherContentFromURL(): IOException creating URL to query. Trace:"+ex.getMessage());
        }

        return weatherCVList;
    }

    private static List<ContentValues> getForecastContentFromURL(String cityid, String prefValueUnits){

        List<ContentValues> forecastCVList = new ArrayList<ContentValues>();

        /* query openweather for this city's "forecast" data
         *  Add this forecast data into the ContentValues <List>
         */
        Uri forecastQueryUri = OpenWeatherUtils.getBaseForecastUri();
        forecastQueryUri = OpenWeatherUtils.getUriWithLocationId(forecastQueryUri, cityid);
        forecastQueryUri = OpenWeatherUtils.getUriWithResultType(forecastQueryUri, "accurate");
        try {
            URL url = new URL(forecastQueryUri.toString());
            Log.d(TAG,"getForecastContentFromURL(): URI for querying forecast data for this city : "+ forecastQueryUri.toString());

            JsonReader reader = NetworkUtils.getResponseFromURL(url);

            // get the array of ContentValues from the response data from OpenWeatherApi
            forecastCVList = JSONUtils.readForecastResponseJSON(reader);
            Log.d(TAG,"getForecastContentFromURL(): total "+forecastCVList.size()+" forecast info from JSONResponse's Reader, for city-id="+cityid);
        }catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.w(TAG, "getForecastContentFromURL(): MalformedURLException creating URL to query forecast. Trace:" + ex.getMessage());
        }catch(IOException ex){
            ex.printStackTrace();
            Log.w(TAG,"getForecastContentFromURL(): IOException creating URL to query forecast. Trace:"+ex.getMessage());
        }

        return forecastCVList;
    }
}

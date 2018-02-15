package com.myhome.weatherapp.myweatherapp.openweathermap;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.myhome.weatherapp.myweatherapp.provider.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by emuanir on 1/30/2018.
 */

public class OpenWeatherUtils {

    private static final String TAG = "DEBUG(OWUtils):";

    /**
     * Passed in argument is a JSONObject either from weather(current) or from forecast [list]
     * The method walks through each key needed (and optional) by the weather database and gets or opts the value in
     * Create a contentValue object with each mandatory and optional parameter in the DBContract database
     * Create an array of ContentValue objects for each entry in the list of weather data received.
     */
    public static ContentValues[] getContentValuesFromJSONDump(JSONObject jsonObject){
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

        try {
            if (jsonObject.has(weatherContract.weatherObject.LIST)) {
                // this is a forecast result with a list of weather entries
                JSONArray weatherEntriesList = jsonObject.getJSONArray(weatherContract.weatherObject.LIST);

                JSONObject cityObject = jsonObject.getJSONObject(weatherContract.WEATHER_CITY);
                int cityid = cityObject.getInt(weatherContract.cityObject.CITY_ID);


                for (int i=0;i<weatherEntriesList.length();i++){
                    JSONObject o = weatherEntriesList.getJSONObject(i);
                    ContentValues cv = getContentValuesFromJSONObject(o);
                    cv.put(DBContract.WeatherEntry.CITY_ID, cityid);
                    contentValuesArrayList.add(cv);
                }
            }else{
                // this is a current weather data dump; get ContentValues directly from the root object
                ContentValues cv = getContentValuesFromJSONObject(jsonObject);
                cv.put(DBContract.WeatherEntry.CITY_ID, jsonObject.getInt(weatherContract.cityObject.CITY_ID));
                contentValuesArrayList.add(cv);
            }
        }catch (Exception e){
            Log.w(TAG,"getContentValuesFromJSONDump(): Exception generated when parsing through json dump. Trace:"+e.getMessage());
            e.printStackTrace();
            return null;
        }

        ContentValues[] cvArray = new ContentValues[contentValuesArrayList.size()];
        for (int i=0;i<contentValuesArrayList.size();i++){
            cvArray[i] = contentValuesArrayList.get(i);
        }

        return cvArray;
    }

    @Nullable
    public static ContentValues getContentValuesFromJSONObject(JSONObject jsonObject){
        if (jsonObject==null) return null;

        ContentValues values = new ContentValues();
        try{
            // parse out each json key as per weatherContract
            values.put(DBContract.WeatherEntry.TIMESTAMP, jsonObject.getInt(weatherContract.weatherObject.TIMESTAMP));

            JSONObject weatherMain = jsonObject.getJSONObject(weatherContract.weatherObject.WEATHER_MAIN);
            values.put(DBContract.WeatherEntry.TEMPERATURE, weatherMain.getDouble(weatherContract.weatherObject.weatherMain.TEMPERATURE));
            values.put(DBContract.WeatherEntry.TEMPERATURE_MIN, weatherMain.getDouble(weatherContract.weatherObject.weatherMain.TEMPERATURE_MIN));
            values.put(DBContract.WeatherEntry.TEMPERATURE_MAX, weatherMain.getDouble(weatherContract.weatherObject.weatherMain.TEMPERATURE_MAX));
            double pressure = weatherMain.optDouble(weatherContract.weatherObject.weatherMain.PRESSURE,0.0);
            if (pressure==0.0){
                 double pressure_alt = weatherMain.optDouble(weatherContract.weatherObject.weatherMain.PRESSURE_ALT,0.0);
                 if (pressure_alt==0.0){
                     throw new JSONException("Non-existant or invalid key for pressure");
                 }else{
                     pressure = pressure_alt;
                 }
            }
            values.put(DBContract.WeatherEntry.PRESSURE, pressure);
            values.put(DBContract.WeatherEntry.HUMIDITY, weatherMain.getDouble(weatherContract.weatherObject.weatherMain.HUMIDITY));


            JSONObject weatherExtras = jsonObject.getJSONArray(weatherContract.weatherObject.WEATHER_EXTRAS).getJSONObject(0);
            values.put(DBContract.WeatherEntry.WEATHER_ID, weatherExtras.getInt(weatherContract.weatherObject.weatherExtras.ID));
            values.put(DBContract.WeatherEntry.WEATHER_GROUP, weatherExtras.getString(weatherContract.weatherObject.weatherExtras.MAIN));
            values.put(DBContract.WeatherEntry.WEATHER_ICON, weatherExtras.getString(weatherContract.weatherObject.weatherExtras.ICON));
            values.put(DBContract.WeatherEntry.WEATHER_DESC, weatherExtras.getString(weatherContract.weatherObject.weatherExtras.DESCRIPTION));


            JSONObject cloudsObject = jsonObject.getJSONObject(weatherContract.weatherObject.CLOUDS);
            if (cloudsObject!=null){
                values.put(DBContract.WeatherEntry.CLOUDS, cloudsObject.getDouble(weatherContract.weatherObject.weatherClouds.CLOUDS));
            }

            JSONObject windObject = jsonObject.getJSONObject(weatherContract.weatherObject.WINDS);
            if (windObject!=null){
                values.put(DBContract.WeatherEntry.WIND_DEGREE, weatherContract.weatherObject.weatherWinds.WINDS_DEGREE);
                values.put(DBContract.WeatherEntry.WIND_SPEED, weatherContract.weatherObject.weatherWinds.WINDS_SPEED);
            }

            JSONObject rainObject = jsonObject.getJSONObject(weatherContract.weatherObject.RAIN);
            if (jsonObject!=null){
                values.put(DBContract.WeatherEntry.RAIN, weatherContract.weatherObject.weatherRain.RAINS);
            }

            JSONObject snowObject = jsonObject.getJSONObject(weatherContract.weatherObject.SNOW);
            if (snowObject!=null){
                values.put(DBContract.WeatherEntry.SNOW, weatherContract.weatherObject.weatherSnow.SNOW);
            }

            // check if the "city" key exists, else get the sub-keys of "city" from the main jsonObject

            return values;
        }catch (JSONException e){
            Log.d(TAG,"getContentValuesFromJSONObject() caught an exception when parsing a jsonobject. Trace:");
            e.printStackTrace();
        }

        return null;
    }

    public static ContentValues[] getContentValuesFromJSONArray(JSONArray jsonArray){

        ContentValues[] cvArray = new ContentValues[jsonArray.length()];
        for (int i=0;i<jsonArray.length();i++){
            cvArray[i] = getContentValuesFromJSONObject(jsonArray.optJSONObject(i));
        }
        return cvArray;
    }

    public static Uri getBaseWeatherUri(){
        Uri uri = weatherContract.WeatherApiContract.BASE_WEATHER_URI.buildUpon()
                .appendQueryParameter(weatherContract.WeatherApiContract.KEY_API_KEY, weatherContract.APPID)
                .build();
        return uri;
    }

    public static Uri getBaseForecastUri(){
        Uri uri = weatherContract.WeatherApiContract.BASE_FORECAST_URI.buildUpon()
                .appendQueryParameter(weatherContract.WeatherApiContract.KEY_API_KEY, weatherContract.APPID)
                .build();
        return uri;
    }

    public static Uri getUriWithLocationId(Uri uri, String locationid){
        if (locationid==null) return null;

        Uri responseuri = uri.buildUpon()
                .appendQueryParameter(weatherContract.WeatherApiContract.KEY_LOCATION_ID, locationid)
                .build();
        return responseuri;
    }

    public static Uri getUriWithUnitType(Uri uri, String unitType){
        if (unitType==null) return uri;

        return uri.buildUpon()
                .appendQueryParameter(weatherContract.WeatherApiContract.KEY_UNITS, unitType)
                .build();
    }

    public static Uri getUriWithLocationName(Uri uri, String locationName){
        if (locationName==null) return null;

        return uri.buildUpon()
                .appendQueryParameter(weatherContract.WeatherApiContract.KEY_LOCATION_NAME, locationName)
                .build();
    }

    public static Uri getUriWithResultType(Uri uri, String resultType){
        if (resultType==null) return null;

        return uri.buildUpon()
                .appendQueryParameter(weatherContract.WeatherApiContract.KEY_RESULT_TYPE, resultType)
                .build();
    }

    public static int getImageResourceIdentifier(Context context, String resourceName){
        return context.getResources().getIdentifier("ic_"+resourceName, "drawable", context.getPackageName());
    }

}

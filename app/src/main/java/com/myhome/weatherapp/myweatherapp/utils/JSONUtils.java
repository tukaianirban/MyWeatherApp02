package com.myhome.weatherapp.myweatherapp.utils;

import android.content.ContentValues;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.myhome.weatherapp.myweatherapp.openweathermap.weatherContract;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by emuanir on 1/29/2018.
 */

public class JSONUtils {

    private static final String TAG="DEBUG(JSONUtils):";

    public static JSONArray readCitiesJSONArray(JsonReader reader){

        JSONArray jsonArray = new JSONArray();

        try {
            reader.beginArray();
            while (reader.hasNext()){
                JSONObject jObject = readCityJSONObject(reader);
                if (jObject!=null)
                    jsonArray.put(jObject);
            }
            reader.endArray();

        }catch(IOException ex){
            ex.printStackTrace();
        }

        // could be an incomplete Array of json objects, if exception occurs half-way through parsing the whole inputstream
        return jsonArray;
    }

    public static JSONObject readCityJSONObject(JsonReader reader){

        JSONObject jsonObject = new JSONObject();
        try {
            reader.beginObject();
            while (reader.hasNext()) {

                // extract only city-id, name, country-code from the json dump keys
                String name = reader.nextName();
                switch(name){
                    case "id":
                        jsonObject.put("id",reader.nextLong());
                        ////Log.d(TAG,"readCityJSONObject(): read in city id="+jsonObject.get("id"));
                        break;
                    case "country":
                        jsonObject.put("country", reader.nextString());
                        break;
                    case "name":
                        jsonObject.put("name", reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            return jsonObject;

        }catch(IOException ex){
            ex.printStackTrace();
            return null;                // either return a complete object, or return a null
        }catch(JSONException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static List<ContentValues> readForecastResponseJSON(JsonReader reader){

        List<ContentValues> weatherContentValues = new ArrayList<ContentValues>();
        try{

            //remove the outermost Object-Begin
            reader.beginObject();

            while(reader.hasNext()){
                String name=reader.nextName();
                switch(name){
                    case weatherContract.weatherObject.LIST:

                        /* iterate through each element in the 'list' json-array and collect each object into individual ContentValues
                           add each of these contentValues into a List
                        */
                        reader.beginArray();
                        while (reader.hasNext()){
                            ContentValues values = readForecastJSONObject(reader);
                            weatherContentValues.add(values);
                            ////Log.d(TAG,"readForecastResponseJSON(): New Forecast info entry="+values.toString()+" keys count="+values.size());
                            ////Log.d(TAG,"readForecastResponseJSON(): Total forecast count for this city = "+weatherContentValues.size());
                        }
                        reader.endArray();
                        break;
                    case "cnt":
                        //Log.d(TAG,"readForecastResponseJSON(): Count of returned forecast entries from OpenWeather="+reader.nextInt());
                        break;
                    default:
                        //Log.d(TAG,"readForecastResponseJSON(): skipping key:"+name);
                        reader.skipValue();
                }
                //Log.d(TAG,"readForecastResponseJSON(): With key:"+name+" contentValues' key count="+weatherContentValues.size());
            }
            //remove the outermost Object-End
            reader.endObject();

        }catch(IOException ex){}
        return weatherContentValues;
    }

    public static ContentValues readWeatherResponseJSON(JsonReader reader){
        ContentValues contentValues = new ContentValues();
        try{
            reader.beginObject();
            while(reader.hasNext()){
                String name = reader.nextName();

                switch(name){
                    case weatherContract.weatherObject.TIMESTAMP:
                        contentValues.put(DBContract.WeatherEntry.TIMESTAMP, reader.nextLong());
                        break;
                    case weatherContract.weatherObject.WEATHER_EXTRAS:
                        contentValues = readWeatherObjectWeather(reader, contentValues);
                        break;
                    case weatherContract.weatherObject.WEATHER_MAIN:
                        contentValues = readWeatherObjectMain(reader, contentValues);
                        break;
                    case weatherContract.weatherObject.WINDS:
                        contentValues = readWeatherObjectWind(reader, contentValues);
                        break;
                    case weatherContract.weatherObject.CLOUDS:
                        contentValues = readWeatherObjectClouds(reader, contentValues);
                        break;
                    case weatherContract.weatherObject.RAIN:
                        contentValues = readWeatherObjectRain(reader, contentValues);
                        break;
                    case weatherContract.weatherObject.SNOW:
                        contentValues = readWeatherObjectSnow(reader, contentValues);
                        break;
                    default:
                        //Log.d(TAG,"readWeatherResponseJSON(): Skipping key:"+name);
                        reader.skipValue();
                }
            }
        }catch(IOException ex){}

        return contentValues;
    }

    public static ContentValues readForecastJSONObject(JsonReader reader){

        ContentValues values = new ContentValues();
        try {
            reader.beginObject();

            while (reader.hasNext()){
                String name = reader.nextName();

                switch (name){
                    case weatherContract.weatherObject.TIMESTAMP:
                        values.put(DBContract.WeatherEntry.TIMESTAMP,reader.nextLong());
                        break;
                    case weatherContract.weatherObject.WEATHER_MAIN:
                        values = readWeatherObjectMain(reader, values);
                        break;
                    case weatherContract.weatherObject.WEATHER_EXTRAS:
                        values = readWeatherObjectWeather(reader, values);
                        break;
                    case weatherContract.weatherObject.CLOUDS:
                        values = readWeatherObjectClouds(reader, values);
                        break;
                    case weatherContract.weatherObject.WINDS:
                        values = readWeatherObjectWind(reader, values);
                        break;
                    case weatherContract.weatherObject.RAIN:
                        values = readWeatherObjectRain(reader, values);
                        break;
                    case weatherContract.weatherObject.SNOW:
                        values = readWeatherObjectSnow(reader, values);
                        break;
                    default:
                        //Log.d(TAG,"readForecastJSONObject(): Skipping key="+name);
                        reader.skipValue();
                }
            }
            reader.endObject();
        }catch (IOException ex){}

        return values;
    }

    public static ContentValues readWeatherObjectMain(JsonReader reader, ContentValues contentValues){

        try{
            reader.beginObject();
            while (reader.hasNext()){
                switch(reader.nextName()){
                    case weatherContract.weatherObject.weatherMain.TEMPERATURE:
                        contentValues.put(DBContract.WeatherEntry.TEMPERATURE, reader.nextDouble());
                        break;
                    case weatherContract.weatherObject.weatherMain.TEMPERATURE_MIN:
                        contentValues.put(DBContract.WeatherEntry.TEMPERATURE_MIN, reader.nextDouble());
                        break;
                    case weatherContract.weatherObject.weatherMain.TEMPERATURE_MAX:
                        contentValues.put(DBContract.WeatherEntry.TEMPERATURE_MAX, reader.nextDouble());
                        break;
                    case weatherContract.weatherObject.weatherMain.PRESSURE:
                        // if 'pressure' key is already extracted, overwrite it with contents of 'grnd_level' key
                        contentValues.put(DBContract.WeatherEntry.PRESSURE, reader.nextDouble());
                        break;
                    case weatherContract.weatherObject.weatherMain.PRESSURE_ALT:
                        // extract/use the 'pressure' key only if the grnd_level key is not already present
                        if (!contentValues.containsKey(weatherContract.weatherObject.weatherMain.PRESSURE))
                            contentValues.put(DBContract.WeatherEntry.PRESSURE, reader.nextDouble());
                        break;
                    case weatherContract.weatherObject.weatherMain.HUMIDITY:
                        contentValues.put(DBContract.WeatherEntry.HUMIDITY, reader.nextDouble());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }catch(IOException ex){

        }
        return contentValues;
    }

    public static ContentValues readWeatherObjectWeather(JsonReader reader, ContentValues contentValues){

        try{
            reader.beginArray();
            reader.beginObject();

            while(reader.hasNext()){
                switch(reader.nextName()){
                    case weatherContract.weatherObject.weatherExtras.DESCRIPTION:
                        contentValues.put(DBContract.WeatherEntry.WEATHER_DESC, reader.nextString());
                        break;
                    case weatherContract.weatherObject.weatherExtras.ID:
                        contentValues.put(DBContract.WeatherEntry.WEATHER_ID, reader.nextInt());
                        break;
                    case weatherContract.weatherObject.weatherExtras.MAIN:
                        contentValues.put(DBContract.WeatherEntry.WEATHER_GROUP, reader.nextString());
                        break;
                    case weatherContract.weatherObject.weatherExtras.ICON:
                        contentValues.put(DBContract.WeatherEntry.WEATHER_ICON, reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            reader.endArray();
        }catch (IOException ex){}

        return contentValues;
    }

    public static ContentValues readWeatherObjectClouds(JsonReader reader, ContentValues contentValues){
        try {
            reader.beginObject();

            while(reader.hasNext()){
                switch(reader.nextName()){
                    case weatherContract.weatherObject.weatherClouds.CLOUDS:
                        contentValues.put(DBContract.WeatherEntry.CLOUDS, reader.nextDouble());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }catch (IOException ex){}

        return contentValues;
    }

    public static ContentValues readWeatherObjectWind(JsonReader reader, ContentValues contentValues){
        try{
            reader.beginObject();

            while(reader.hasNext()){
                switch(reader.nextName()){
                    case weatherContract.weatherObject.weatherWinds.WINDS_DEGREE:
                        contentValues.put(DBContract.WeatherEntry.WIND_DEGREE, reader.nextDouble());
                        break;
                    case weatherContract.weatherObject.weatherWinds.WINDS_SPEED:
                        contentValues.put(DBContract.WeatherEntry.WIND_SPEED, reader.nextDouble());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }catch(IOException ex){}

        return contentValues;
    }

    public static ContentValues readWeatherObjectRain(JsonReader reader, ContentValues contentValues){
        try{
            reader.beginObject();
            while(reader.hasNext()){
                switch(reader.nextName()){
                    case weatherContract.weatherObject.weatherRain.RAINS:
                        contentValues.put(DBContract.WeatherEntry.RAIN, reader.nextDouble());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }catch(IOException ex){}

        return contentValues;
    }

    public static ContentValues readWeatherObjectSnow(JsonReader reader, ContentValues contentValues){
        try{
            reader.beginObject();
            while(reader.hasNext()){
                switch(reader.nextName()) {
                    case weatherContract.weatherObject.weatherSnow.SNOW:
                        contentValues.put(DBContract.WeatherEntry.SNOW, reader.nextDouble());
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        }catch (IOException ex){}

        return contentValues;
    }
}

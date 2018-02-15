package com.myhome.weatherapp.myweatherapp.utils;

import android.content.ContentValues;
import android.os.Bundle;

import com.myhome.weatherapp.myweatherapp.provider.DBContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by emuanir on 2/2/2018.
 */

public class conversionUtils {

    // the epoch time returned from OpenWeatherMap is in seconds.
    // SimpleDateFormat requires Date created in milliSeconds. SO MULTIPLY BY 1000.
    public static String getTimeHoursMinutesFromStamp(long millis){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date(millis*1000));
    }

    public static String getTimeDay(long millis){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd/MM");
        return simpleDateFormat.format(new Date(millis*1000));
    }

    /* given the epoch time, the method returns a human-readable form of the date/time
      * Returned format is different depending on whether the date is today or in the past
     */
    public static String getReadableTimeFromStamp(String timeString){
        if (timeString==null) return "";

        long millis = Long.parseLong(timeString);
        Calendar now = Calendar.getInstance();
        Calendar dateDay = Calendar.getInstance();
        dateDay.setTimeInMillis(millis);

        if ((now.get(Calendar.DATE) - dateDay.get(Calendar.DATE))>0){
            // the passed in date is yesterday or earlier
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM");
            return simpleDateFormat.format(new Date(millis));
        }else{
            // the passed in date is during today
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            return simpleDateFormat.format(new Date(millis));
        }
    }

    // conversion from Kelvin to Celsius temperature units
    public static Double getMetricTemperatureFromKelvin(Double temperatureKelvin){
        return temperatureKelvin - 273.15;
    }

    // conversion from Kelvin to Fahrenheit temperature units
    public static Double getImperialTemperatureFromKelvin(Double temperatureKelvin){
        Double celTemp = getMetricTemperatureFromKelvin(temperatureKelvin);
        return (9.0 * celTemp)/5.0 + 32.0;
    }

    public static Bundle getBundleFromContentValues(ContentValues values){

        Bundle b = new Bundle();
        for (String key: values.keySet()){
            String val = String.valueOf(values.get(key));
            switch (key){
                case DBContract.WeatherEntry.TEMPERATURE:
                case DBContract.WeatherEntry.TEMPERATURE_MAX:
                case DBContract.WeatherEntry.TEMPERATURE_MIN:
                case DBContract.WeatherEntry.WEATHER_ICON:
                case DBContract.WeatherEntry.WEATHER_DESC:
                case DBContract.WeatherEntry.RAIN:
                case DBContract.WeatherEntry.WIND_SPEED:
                case DBContract.WeatherEntry.SNOW:
                    b.putString(key, val);
                    break;
                default:
                    continue;
            }
        }
        return b;
    }
}

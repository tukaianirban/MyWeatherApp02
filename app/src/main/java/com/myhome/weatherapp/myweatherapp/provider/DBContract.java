package com.myhome.weatherapp.myweatherapp.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by emuanir on 1/29/2018.
 */

public class DBContract {

    public static final String AUTHORITY = "com.myhome.weatherapp.myweatherapp";

    public final static String DATABASE_NAME = "weatherdb.db";
    public static final Uri BASE_URI = Uri.parse("content://"+ AUTHORITY);

    public static final String PATH_CITY = "city";
    public static final String PATH_WEATHER = "weather";

    public static final Uri CITY_CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_CITY).build();
    public static final Uri WEATHER_CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_WEATHER).build();

    public static final String TABLE_WEATHER = "weathertable";
    public static final String TABLE_CITYLIST = "citylist";

    public static class WeatherEntry implements BaseColumns{
        public static final String CITY_ID = "city_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String TEMPERATURE = "temp";
        public static final String TEMPERATURE_MIN = "temp_min";
        public static final String TEMPERATURE_MAX = "temp_max";
        public static final String PRESSURE = "grnd_level";
        public static final String HUMIDITY = "humidity";
        public static final String WEATHER_ID = "weather_id";
        public static final String WEATHER_GROUP = "weather_group";
        public static final String WEATHER_DESC = "weather_desc";
        public static final String WEATHER_ICON = "weather_icon";
        public static final String CLOUDS = "clouds";
        public static final String WIND_SPEED = "wind_speed";
        public static final String WIND_DEGREE = "wind_degree";
        public static final String RAIN = "rain";
        public static final String SNOW = "snow";
    }

    public static class WeatherExtras {

    }

    public static class CityList implements BaseColumns{
        public static final String ID = "id";
        public static final String CITY_NAME = "name";
        public static final String COUNTRY_CODE = "country";
    }
}

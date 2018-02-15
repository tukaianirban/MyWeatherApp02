package com.myhome.weatherapp.myweatherapp.openweathermap;

import android.net.Uri;

/**
 * Created by emuanir on 1/29/2018.
 */

public final class weatherContract {

    public final static String APPID="<<<<YOUR OPENWEATHERAPI KEY HERE>>>>>>>";

    public static final class WeatherApiContract{
        public static final Uri BASE_WEATHER_URI = Uri.parse("http://api.openweathermap.org/data/2.5/weather");
        public static final Uri BASE_FORECAST_URI = Uri.parse("http://api.openweathermap.org/data/2.5/forecast");
        public static final String KEY_LOCATION_NAME = "q";
        public static final String KEY_LOCATION_ID = "id";
        public static final String KEY_API_KEY = "appid";
        public static final String KEY_UNITS = "units";
        public static final String KEY_RESULT_TYPE = "type";
    }


    public static final String WEATHER_CITY = "city";
    public static final class cityObject{
        public static final String CITY_ID = "id";
        public static final String CITY_NAME = "name";
        public static final String COUNTRY_CODE = "country";
    }

    public static final class weatherObject{
        public static final String TIMESTAMP = "dt";
        public static final String LIST = "list";

        public static final String WEATHER_MAIN = "main";
        public static final class weatherMain{
            public static final String TEMPERATURE = "temp";
            public static final String TEMPERATURE_MIN = "temp_min";
            public static final String TEMPERATURE_MAX = "temp_max";
            public static final String PRESSURE = "grnd_level";
            public static final String PRESSURE_ALT = "pressure";
            public static final String HUMIDITY = "humidity";
        }
        public static final String WEATHER_EXTRAS = "weather";
        public static final class weatherExtras{
            public static final String ID = "id";
            public static final String MAIN = "main";
            public static final String DESCRIPTION = "description";
            public static final String ICON = "icon";
        }

        public static final String CLOUDS = "clouds";
        public static final class weatherClouds{
            public static final String CLOUDS = "all";
        }

        public static final String WINDS = "wind";
        public static final class weatherWinds{
            public static final String WINDS_SPEED = "speed";
            public static final String WINDS_DEGREE = "deg";
        }

        public static final String RAIN = "rain";
        public static final class weatherRain{
            public static final String RAINS = "3h";
        }

        public static final String SNOW = "snow";
        public static final class weatherSnow{
            public static final String SNOW = "3h";
        }

    }
}

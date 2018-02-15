package com.myhome.weatherapp.myweatherapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by emuanir on 1/30/2018.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DEBUG(DBHelper):";

    private final static int DATABASE_VERSION = 1;

    public DBHelper(Context context){
        super(context, DBContract.DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG,"DBHelper(): constructor created");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        Log.d(TAG,"onCreate(): called");
        final String SQL_CREATE_TABLE_WEATHER = "CREATE TABLE " + DBContract.TABLE_WEATHER + "("
                + DBContract.WeatherEntry._ID + " INTEGER PRIMARY KEY, "
                + DBContract.WeatherEntry.CITY_ID + " INT NOT NULL, "
                + DBContract.WeatherEntry.TIMESTAMP + " INT NOT NULL, "
                + DBContract.WeatherEntry.TEMPERATURE + " REAL NOT NULL, "
                + DBContract.WeatherEntry.TEMPERATURE_MIN + " REAL NOT NULL, "
                + DBContract.WeatherEntry.TEMPERATURE_MAX + " REAL NOT NULL, "
                + DBContract.WeatherEntry.PRESSURE + " REAL NOT NULL, "
                + DBContract.WeatherEntry.HUMIDITY + " REAL NOT NULL, "
                + DBContract.WeatherEntry.WEATHER_ID + " INT NOT NULL, "
                + DBContract.WeatherEntry.WEATHER_GROUP + " TEXT NOT NULL, "
                + DBContract.WeatherEntry.WEATHER_DESC + " TEXT NOT NULL, "
                + DBContract.WeatherEntry.WEATHER_ICON + " TEXT NOT NULL, "
                + DBContract.WeatherEntry.CLOUDS + " REAL, "
                + DBContract.WeatherEntry.WIND_SPEED + " REAL, "
                + DBContract.WeatherEntry.WIND_DEGREE + " REAL, "
                + DBContract.WeatherEntry.RAIN + " REAL, "
                + DBContract.WeatherEntry.SNOW + " REAL"
                + ");";

        final String SQL_CREATE_TABLE_CITY = "CREATE TABLE " + DBContract.TABLE_CITYLIST + "("
                + DBContract.CityList._ID + " INTEGER PRIMARY KEY, "
                + DBContract.CityList.ID + " INT NOT NULL, "
                + DBContract.CityList.CITY_NAME + " TEXT NOT NULL, "
                + DBContract.CityList.COUNTRY_CODE + " TEXT NOT NULL"
                + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_WEATHER);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_CITY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(TAG,"onUpgrade(): called ");
        if (newVersion!=oldVersion){
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+DBContract.TABLE_WEATHER);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+DBContract.TABLE_CITYLIST);
            onCreate(sqLiteDatabase);
        }
    }
}

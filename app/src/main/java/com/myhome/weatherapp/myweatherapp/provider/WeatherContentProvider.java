package com.myhome.weatherapp.myweatherapp.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Sample URI calls for ContentProvider operations:
 * content:///com.myhome.weatherapp.myweatherapp/weather    => returns entire weather table
 * content:///com.myhome.weatherapp.myweatherapp/weather/1250074    => returns weather entries for city id 1250074
 * content:///com.myhome.weatherapp.myweatherapp/city       => returns entire city table
 * content:///com.myhome.weatherapp.myweatherapp/city/1250074       => returns city info for city id 1250074
 *
 */

public class WeatherContentProvider extends ContentProvider {

    private static final String TAG = "DEBUG(Provider):";

    // member variables
    private static final int CODE_WEATHER_ALL = 100, CODE_WEATHER_CITY_ID = 101, CODE_WEATHER_CITY_NAME = 102;
    private static final int CODE_CITY_ALL = 200, CODE_CITY_DETAILS_ID=201, CODE_CITY_DETAILS_NAME=202;
    private static Context mContext;
    private static DBHelper mDBHelper;

    public static final UriMatcher getUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_WEATHER, CODE_WEATHER_ALL);                     //uri to list all weather entries
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_WEATHER+"/#", CODE_WEATHER_CITY_ID);         // uri to list weather entries for a given city ID
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_WEATHER+"/*", CODE_WEATHER_CITY_NAME);      // uri to list weather entries for a given city name

        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_CITY, CODE_CITY_ALL);                           // uri to list all city entries
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_CITY+"/#", CODE_CITY_DETAILS_ID);              // uri to list details of a given city ID
        uriMatcher.addURI(DBContract.AUTHORITY, DBContract.PATH_CITY+"/*", CODE_CITY_DETAILS_NAME);         // uri to list details of a given city name

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG,"onCreate() called");

        mContext = getContext();
        mDBHelper = new DBHelper(mContext);

        return true;                        // consumed here
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {

        final SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;
        List<String> pathSegments;
        switch (getUriMatcher().match(uri)){
            case CODE_WEATHER_ALL:
                // query for all the weather contents
                cursor = db.query(DBContract.TABLE_WEATHER,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
                Log.d(TAG,"query(): query for all weather info returned cursor with length="+cursor.getCount());
                break;
            case CODE_WEATHER_CITY_ID:
                // URI should be looking like : content:///com.myhome.weatherapp.myweatherapp/weather/1250074
                 pathSegments = uri.getPathSegments();
                if (pathSegments.get(1)!=null){
                    cursor = db.query(DBContract.TABLE_WEATHER,
                            null,
                            DBContract.WeatherEntry.CITY_ID+"=?",
                            new String[]{pathSegments.get(1)},
                            null,
                            null,
                            DBContract.WeatherEntry.TIMESTAMP);
                    Log.d(TAG,"query(): fetching weather details for city id="+
                            pathSegments.get(1)+" Result has "+cursor.getCount()+" rows");
                }else{
                    cursor=null;
                    Log.w(TAG,"query(): query for city id, has no valid city id in it");
                }
                break;
            case CODE_WEATHER_CITY_NAME:
                // URI should be looking like : content:///com.myhome.weatherapp.myweatherapp/weather/Kolkata,IN
                // !!!!!!! NOT IMPLEMENTED YET
                Log.e(TAG,"query(): query by city name is not implemented yet");
                cursor=null;
                break;

            case CODE_CITY_ALL:
                // URI should be looking like : content:///com.myhome.weatherapp.myweatherapp/city
                cursor = db.query(DBContract.TABLE_CITYLIST,
                            null,
                            null,
                        null,
                        null,
                        null,
                        null);
                Log.d(TAG,"query(): query for all cities returned cursor with length="+cursor.getCount());
                break;
            case CODE_CITY_DETAILS_ID:
                // URI should be looking like : content:///com.myhome.weatherapp.myweatherapp/city/1250074
                pathSegments = uri.getPathSegments();
                if (pathSegments.get(1)!=null) {
                    cursor = db.query(DBContract.TABLE_CITYLIST,
                            null,
                            DBContract.CityList.ID + "=?",
                            new String[]{pathSegments.get(1)},
                            null,
                            null,
                            null);
                    Log.d(TAG, "query(): query for city id=" + pathSegments.get(1) + " returned cursor of size=" + cursor.getCount());
                }else{
                    cursor=null;
                    Log.w(TAG,"query(): query for city id has no valid id in it");
                }
                break;
            case CODE_CITY_DETAILS_NAME:
                // URI should be looking like content:///com.myhome.weatherapp.myweatherapp/city/Kolkata,IN
                pathSegments=uri.getPathSegments();
                if (pathSegments.get(1)!=null){
                    cursor = db.query(DBContract.TABLE_CITYLIST,
                            null,
                            DBContract.CityList.CITY_NAME+"=?",
                            new String[]{pathSegments.get(1)},
                            null,
                            null,
                            null);
                    Log.d(TAG,"query(): query for city name="+pathSegments.get(1)+" returned cursor with size="+cursor.getCount());
                }else{
                    Log.w(TAG,"query(): query with city name contains no valid city name");
                    cursor=null;
                }
                break;
            default:
                Log.w(TAG,"query() called with no unknown URI");
                cursor=null;
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /**
     * Will return an URI equal to the original URI sent to the function if the insert() operation succeeds,
     * else returns a null
     * Passed in URI should match only either CODE_CITY_ALL or CODE_WEATHER_ALL.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Uri responseUri=null;
        switch (getUriMatcher().match(uri)) {
            case CODE_WEATHER_ALL:
                db.beginTransaction();
                try {
                    long rowid = db.insert(DBContract.TABLE_WEATHER, null, contentValues);
                    if (rowid < 0) {
                        Log.d(TAG, "insert(): into weather URI:" + uri.toString() + " resulted in an error");
                        responseUri = null;
                    } else {
                        Log.d(TAG, "insert(): into weather URI:" + uri.toString() + " was done at row=" + rowid);
                        responseUri = uri;
                    }
                    db.setTransactionSuccessful();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    db.endTransaction();
                }
                break;
            case CODE_CITY_ALL:
                db.beginTransaction();
                try {
                    long rowid = db.insert(DBContract.TABLE_CITYLIST, null, contentValues);
                    if (rowid < 0) {
                        Log.d(TAG, "insert(): into city URI:" + uri.toString() + " resulted in an error");
                        responseUri = null;
                    } else {
                        Log.d(TAG, "insert(): into city URI:" + uri.toString() + " was done at row=" + rowid);
                        responseUri = uri;
                    }
                    db.setTransactionSuccessful();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    db.endTransaction();
                }
                break;

            default:
                responseUri=null;
                Log.d(TAG,"insert(): called on unknown URI:"+uri.toString());
        }

        if (responseUri!=null){
            getContext().getContentResolver().notifyChange(responseUri, null);
        }
        db.close();
        return responseUri;
    }

    /**
     *
     * @param uri : Uri for the particular city
     * @param contentValues : Array of ContentValues for a city. Each ContentValue contains 1 weather entry
     * Use Exclusive mode of database transaction so that multiple inserts can be done in 1 method call
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] contentValues) {

        int rowsCount=0;
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        switch(getUriMatcher().match(uri)){
            case CODE_WEATHER_ALL:
                db.beginTransaction();
                for (ContentValues contentValue: contentValues ) {
                    try {
                        if (db.insert(DBContract.TABLE_WEATHER, null, contentValue)>-1)
                            rowsCount++;
                        Log.d(TAG,"bulkInsert(): rows entered for this call is="+rowsCount);
                    } catch (Exception ex) {
                        Log.w(TAG, "bulkInsert(): Exception when inserting weather data into the database.Trace:" + ex.getMessage());
                    }
                }
                // idea is to enter as many rows as possible into the database
                db.setTransactionSuccessful();
                db.endTransaction();

                return rowsCount;
            case CODE_CITY_ALL:
                db.beginTransaction();
                for (ContentValues contentValue: contentValues ) {
                    try {
                        if (db.insert(DBContract.TABLE_CITYLIST, null, contentValue)>-1)
                            rowsCount++;
                    } catch (Exception ex) {
                        Log.w(TAG, "bulkInsert(): Exception when inserting city data into the database.Trace:" + ex.getMessage());
                    }
                }
                // idea is to enter as many rows as possible into the database
                db.setTransactionSuccessful();
                db.endTransaction();

                return rowsCount;
            default:
                Log.d(TAG,"bulkInsert(): Unsuppored Uri used in call to bulkInsert()");
                return super.bulkInsert(uri, contentValues);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int rowCount = 0;
        List<String> pathSegments;

        switch(getUriMatcher().match(uri)){
            case CODE_WEATHER_ALL:
                rowCount = db.delete(DBContract.TABLE_WEATHER, null, null);
                // notify all applications of URIs pointing to the entire weather info table about the data source change
                if (rowCount>0)
                    getContext().getContentResolver().notifyChange(uri, null);
                Log.d(TAG,"delete(): resulted in deleting "+rowCount+" rows");
                break;
            case CODE_WEATHER_CITY_ID:
                pathSegments = uri.getPathSegments();
                if (pathSegments.get(1)!=null){
                    rowCount = db.delete(DBContract.TABLE_WEATHER,
                            DBContract.WeatherEntry.CITY_ID+"=?",
                            new String[]{pathSegments.get(1)});
                    // notify users of URI pointing to whole weather table (not just the particular city indicated in this one) about the change in data source
                    if (rowCount>0)
                        getContext().getContentResolver().notifyChange(DBContract.WEATHER_CONTENT_URI, null);

                    Log.d(TAG,"delete(): called on Weather URI for city id="+pathSegments.get(1)
                            +" resulted in deleting "+rowCount+" rows");
                }else{
                    rowCount=0;
                    Log.w(TAG,"delete(): called on Weather table with invalid or no city-id");
                }
                break;
            case CODE_WEATHER_CITY_NAME:
                Log.e(TAG,"delete(): with city-name is not implemented yet");
                rowCount=0;
                break;
            case CODE_CITY_ALL:
                rowCount = db.delete(DBContract.TABLE_CITYLIST, null, null);
                if (rowCount>0)
                    getContext().getContentResolver().notifyChange(DBContract.CITY_CONTENT_URI, null);

                Log.d(TAG,"delete(): resulted in deleting "+ rowCount+ " rows ");
                break;
            case CODE_CITY_DETAILS_ID:
                pathSegments = uri.getPathSegments();
                if (pathSegments.get(1)!=null){
                    rowCount = db.delete(DBContract.TABLE_CITYLIST,
                            DBContract.CityList.ID+"=?",
                            new String[]{pathSegments.get(1)});
                    // notify all applications of the entire city-info table (not just this particular city-id) that data source has changed
                    if (rowCount>0)
                        getContext().getContentResolver().notifyChange(DBContract.CITY_CONTENT_URI, null);

                    Log.d(TAG,"delete(): called on City Table with city-id resulted in deleting "+ rowCount+ " rows");
                }else{
                    rowCount=0;
                    Log.w(TAG,"delete(): called on City Table with no city-id");
                }
                break;
            case CODE_CITY_DETAILS_NAME:
                pathSegments = uri.getPathSegments();
                if (pathSegments.get(1)!=null){
                    rowCount = db.delete(DBContract.TABLE_CITYLIST,
                            DBContract.CityList.CITY_NAME+"=?",
                            new String[]{pathSegments.get(1)});
                    // notify all applications of the entire city-info table (not just this particular city-id) that data source has changed
                    if (rowCount>0)
                        getContext().getContentResolver().notifyChange(DBContract.CITY_CONTENT_URI, null);

                    Log.d(TAG,"delete(): called on City Table with city-name resulted in deleting "+ rowCount + " rows");
                }else{
                    rowCount=0;
                    Log.w(TAG,"delete(): called on City table with no city-name");
                }
                break;
            default:
                Log.w(TAG,"delete(): called with unknown URI:"+uri.toString());
        }

        return rowCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e(TAG,"update(): not yet implemented");
        Toast.makeText(mContext, "Updation of database not supported yet", Toast.LENGTH_SHORT).show();
        return 0;
    }
}

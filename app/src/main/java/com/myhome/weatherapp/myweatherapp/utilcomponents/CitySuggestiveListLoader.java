package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.JsonReader;
import android.util.Log;

import com.myhome.weatherapp.myweatherapp.openweathermap.weatherContract;
import com.myhome.weatherapp.myweatherapp.utils.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AsyncTaskLoader to load the pre-defined list of cities that are published as "known" cities
 * from OpenWeatherMap.
 * Loaded result is a "Map" object containing 2 keys, one for city-ID, one for city-name
 */

public class CitySuggestiveListLoader extends AsyncTaskLoader<Map<String, Object>> {

    private static final String TAG = "DEBUG(CitySuggLoader):";

    private Context mContext;
    private static final String CITY_LIST_FILENAME = "current.city.list.json";

    // the cached content from the last load of city-info.
    Map<String, Object> mCityDataMap = new HashMap<String, Object>();
    List<String> mCityNamesArray;
    List<String> mCityIdArray;
    List<String> mCountryCodesArray;

    public CitySuggestiveListLoader(Context context, Bundle args){
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onStartLoading() {
        if ((mCityNamesArray!=null && mCityIdArray!=null && mCountryCodesArray!=null) && (mCityNamesArray.size()>0 && mCityIdArray.size()>0 && mCountryCodesArray.size()>0))
            deliverResult(mCityDataMap);
        else
            forceLoad();
    }

    @Override
    public void deliverResult(Map<String, Object> data) {
        if (mCityDataMap!=data)
            mCityDataMap = (Map<String,Object>)data;
        super.deliverResult(mCityDataMap);
    }

    @Override
    public Map<String, Object> loadInBackground() {

        mCityNamesArray = new ArrayList<String>();
        mCityIdArray = new ArrayList<String>();
        mCountryCodesArray = new ArrayList<String>();
        AssetManager assetManager = mContext.getAssets();
        try {
            InputStream inputStream = assetManager.open(CITY_LIST_FILENAME);
            JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            JSONArray cityJSONArray = JSONUtils.readCitiesJSONArray(jsonReader);

            for (int i=0;i<cityJSONArray.length();i++){
                JSONObject jsonObject = cityJSONArray.getJSONObject(i);

                mCityIdArray.add(jsonObject.getString(weatherContract.cityObject.CITY_ID));
                mCityNamesArray.add(jsonObject.getString(weatherContract.cityObject.CITY_NAME));
                mCountryCodesArray.add(jsonObject.getString(weatherContract.cityObject.COUNTRY_CODE));
            }

            // convert the lists (city-names and city-ids) to Arrays of String. Then insert them into the Map.
            Log.d(TAG,"loadInBackground(): loaded "+mCityNamesArray.size()+" city-names and "+mCityIdArray.size()+" city-ids");

            mCityDataMap.put(weatherContract.cityObject.CITY_ID, mCityIdArray.toArray(new String[mCityIdArray.size()]));
            mCityDataMap.put(weatherContract.cityObject.CITY_NAME, mCityNamesArray.toArray(new String[mCityNamesArray.size()]));
            mCityDataMap.put(weatherContract.cityObject.COUNTRY_CODE, mCountryCodesArray.toArray(new String[mCountryCodesArray.size()]));

            return mCityDataMap;
        }catch (IOException ex){
            ex.printStackTrace();
            return null;
        }catch (JSONException ex){
            ex.printStackTrace();
            return null;
        }
    }
}

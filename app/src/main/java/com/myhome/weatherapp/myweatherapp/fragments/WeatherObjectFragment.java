package com.myhome.weatherapp.myweatherapp.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myhome.weatherapp.myweatherapp.R;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;
import com.myhome.weatherapp.myweatherapp.utilcomponents.WeatherDisplayAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherObjectFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DEBUG(WeatherFrag):";

    static String mCityId;
    final int LOADER_CITY_WEATHER = 1024;

    RecyclerView rvWeatherForCity;
    Context mContext;
    WeatherDisplayAdapter mWeatherAdapter;

    public WeatherObjectFragment() {
        // Required empty public constructor
    }

    /**
     * Lifecycle methods of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mCityId = getArguments().getString("id");
        mContext = getContext();
        mWeatherAdapter = new WeatherDisplayAdapter(mContext);



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_object, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        rvWeatherForCity = (RecyclerView) view.findViewById(R.id.rvShowWeatherForCity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvWeatherForCity.setLayoutManager(layoutManager);

        rvWeatherForCity.setAdapter(mWeatherAdapter);

    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume(): called");

        // spin off the AsyncTaskLoader to load weather for this city
        Bundle args = new Bundle();
        args.putString("id", mCityId);
        getLoaderManager().initLoader(LOADER_CITY_WEATHER, args, this);
        Log.d(TAG,"Started loader for  weather info for cityid="+mCityId);

        super.onResume();
    }

    /**
     * Loader's overridden methods
     * this loader returns a Cursor for the weather data for a particular city-id
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG,"onCreateLoader() called for Loader id = "+id);
        String city_id = args.getString("id", null);
        if (city_id==null) return null;

        Log.d(TAG,"onCreateLoader(): called for city_id="+city_id);
        Uri cityUri = DBContract.WEATHER_CONTENT_URI.buildUpon()
                .appendPath(city_id)
                .build();

        return new CursorLoader(getContext(), cityUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mWeatherAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWeatherAdapter.swapCursor(null);
    }
}

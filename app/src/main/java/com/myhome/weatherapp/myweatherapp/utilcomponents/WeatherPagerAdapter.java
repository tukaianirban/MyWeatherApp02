package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.myhome.weatherapp.myweatherapp.fragments.WeatherObjectFragment;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;

/**
 * Adapter class to feed the Main Weather display ViewPager. The cursor behind this Adapter
 * contains the user-selected cities list.
 */

public class WeatherPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "DEBUG(WPagerAdptr):";

    Cursor mCursor;                     // Cursor for the user-selected list of cities
    Context mContext;

    public WeatherPagerAdapter(FragmentManager fragmentManager, Context context){
        super(fragmentManager);
        mContext=context;
    }

    @Override
    public int getCount() {
        if (mCursor!=null) {
            //Log.d(TAG,"getCount(): WeatherPagerAdapter has "+mCursor.getCount()+" items");
            return mCursor.getCount();
        }
        else{
            //Log.d(TAG,"getCount(): WeatherPagerAdapter has 0 records");
            return 0;
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (mCursor!=null && mCursor.getCount()>0){
            Log.d(TAG,"getItem(): mCursor has >0 records");
            if (mCursor.moveToPosition(position)){
                String cityId = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.ID));
                Bundle args = new Bundle();
                args.putString("id", cityId);

                Fragment fragment = new WeatherObjectFragment();
                fragment.setArguments(args);
                Log.d(TAG,"getItem(): returning a fragment for city="+cityId);
                return fragment;
            }
        }
        return null;
    }

    public void swapCursor(Cursor cursor){
        if (cursor!=mCursor){
            Log.d(TAG,"swapCursor() called with new data");
            mCursor = cursor;
            if (mCursor instanceof Cursor)
                Log.d(TAG,"swapCursor(): called with cursor of length="+mCursor.getCount());
            this.notifyDataSetChanged();
        }else
            Log.d(TAG,"swapCursor() called with same old data");
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG,"getPageTitle(): called on position "+position);
        if (mCursor!=null && mCursor.getCount()>0){
            if (mCursor.moveToPosition(position)){
                String cityname = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.CITY_NAME));
                return cityname;

            }
        }
        return super.getPageTitle(position);
    }



}

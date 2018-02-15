package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myhome.weatherapp.myweatherapp.R;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;

/**
 * Adapter for feeding the RecyclerView to display the Selected Cities list in the AddCityActivity
 */

public class SelectedCitiesAdapter extends RecyclerView.Adapter<SelectedCitiesAdapter.SelectedCitiesViewHolder>
    implements SelectedCitiesTouchInterface{

    private static final String TAG = "DEBUG(SelCityAdapter):";
    private Context mContext;
    private Cursor mCursor=null;

    public SelectedCitiesAdapter(Context context){
        mContext = context;
    }

    @Override
    public SelectedCitiesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.viewholder_addcityactivity_selected_cities, parent, false);

        return new SelectedCitiesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectedCitiesViewHolder holder, int position) {

        if (!(mCursor instanceof Cursor)) return;

        if (mCursor.getCount()<1) return;

        if (mCursor.moveToPosition(position)){
            String city_name = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.CITY_NAME));
            String country_code = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.COUNTRY_CODE));
            String city_id = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.ID));

            holder.tvSelectedCities_CityName.setText(city_name);
            holder.tvSelectedCities_CountryCode.setText(country_code);
            holder.itemView.setTag(city_id);
        }else{
            Log.d(TAG,"onBindViewHolder(): called on position="+ position + " where Cursor has no data");
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor!=null)
            return mCursor.getCount();
        else
            return 0;
    }

    // triggered when the users swipes left / right on the ViewHolder
    @Override
    public void onItemDismiss(int position) {
        Log.d(TAG,"onItemDismiss(): called on the SelectedCitiesAdapter at position="+position);

        if (mCursor.moveToPosition(position)){
            String city_id = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.ID));
            Uri cityDelUri = DBContract.CITY_CONTENT_URI.buildUpon()
                    .appendPath(city_id)
                    .build();
            int rowcount = mContext.getContentResolver().delete(cityDelUri, null, null);
            Log.d(TAG,"onItemmDismiss(): deleted "+ rowcount+ " city-info records");
        }
    }

    public void swapCursor(Cursor cursor){
        Log.d(TAG,"swapCursor(): called");
        if (cursor!=mCursor){
            Log.d(TAG,"swapCursor(): called with new Cursor contents");
            mCursor = cursor;
            this.notifyDataSetChanged();
        }else
            Log.d(TAG,"swapCursor(): called with same old Cursor contents");
    }

    class SelectedCitiesViewHolder extends RecyclerView.ViewHolder{

        TextView tvSelectedCities_CityName, tvSelectedCities_CountryCode;

        public SelectedCitiesViewHolder(View itemView) {
            super(itemView);

            tvSelectedCities_CityName = itemView.findViewById(R.id.tvSelectedCities_CityName);
            tvSelectedCities_CountryCode = itemView.findViewById(R.id.tvSelectedCities_CountryCode);
        }
    }
}

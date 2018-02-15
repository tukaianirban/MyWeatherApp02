package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myhome.weatherapp.myweatherapp.R;
import com.myhome.weatherapp.myweatherapp.openweathermap.OpenWeatherUtils;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;
import com.myhome.weatherapp.myweatherapp.utils.conversionUtils;

/**
 * This adapter feeds data into each of the fragments displaying weather for a given city
 */

public class WeatherDisplayAdapter extends RecyclerView.Adapter<WeatherDisplayAdapter.WeatherDisplayViewHolder> {

    private static final String TAG = "DEBUG(WeatherAdapter):";

    Context mContext;
    Cursor mCursor;
    String unitTypeLabel, unitType;

    public WeatherDisplayAdapter(Context context){
        mContext = context;

    }

    @Override
    public WeatherDisplayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext==null) Log.d(TAG,"onCreateViewHolder(): Context is null");

        View view;
        if (viewType==R.layout.viewholder_show_weather_entry)
            view = LayoutInflater.from(mContext)
                .inflate(R.layout.viewholder_show_weather_entry, parent, false);
        else
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.viewholder_show_forecast_entry, parent, false);

        return new WeatherDisplayViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0)
            return R.layout.viewholder_show_weather_entry;
        else
            return R.layout.viewholder_show_forecast_entry;
    }

    @Override
    public void onBindViewHolder(WeatherDisplayViewHolder holder, int position) {
        if (mCursor==null || mCursor.getCount()<1)
            return;

        if (mCursor.moveToPosition(position)){
            //weather icon and description
            holder.ivWeatherIcon.setImageResource(
                    OpenWeatherUtils.getImageResourceIdentifier(
                            mContext, mCursor.getString(mCursor.getColumnIndex(DBContract.WeatherEntry.WEATHER_ICON))));
            holder.tvWeatherDescription.setText(mCursor.getString(mCursor.getColumnIndex(DBContract.WeatherEntry.WEATHER_DESC)));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            unitType = sharedPreferences.getString(mContext.getResources().getString(R.string.pref_units_key), null);
            if (unitType==null) return;
            /* Get (Kelvin) weather value from database, convert to user-selected metric type (double),
               convert double-value to String format (adding in deg symbol and metric format character
             */
            Double temperature, temperatureMin, temperatureMax;
            String temperatureFormatted="", temperatureMinFormatted="", temperatureMaxFormatted="";
            if (unitType.equals(mContext.getResources().getString(R.string.pref_units_value_metric))){
                temperature = conversionUtils.getMetricTemperatureFromKelvin(
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.TEMPERATURE)));
                temperatureFormatted = mContext.getString(R.string.format_display_temperature_metric, temperature);
                temperatureMin = conversionUtils.getMetricTemperatureFromKelvin(
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.TEMPERATURE_MIN)));
                temperatureMinFormatted = mContext.getString(R.string.format_display_temperature_metric, temperatureMin);
                temperatureMax = conversionUtils.getMetricTemperatureFromKelvin(
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.TEMPERATURE_MAX)));
                temperatureMaxFormatted = mContext.getString(R.string.format_display_temperature_metric, temperatureMax);
            }else if(unitType.equals(mContext.getResources().getString(R.string.pref_units_value_imperial))) {
                temperature = conversionUtils.getImperialTemperatureFromKelvin(
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.TEMPERATURE)));
                temperatureFormatted = mContext.getString(R.string.format_display_temperature_imperial, temperature);
                temperatureMin = conversionUtils.getImperialTemperatureFromKelvin(
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.TEMPERATURE_MIN)));
                temperatureMinFormatted = mContext.getString(R.string.format_display_temperature_imperial, temperatureMin);
                temperatureMax = conversionUtils.getImperialTemperatureFromKelvin(
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.TEMPERATURE_MAX)));
                temperatureMaxFormatted = mContext.getString(R.string.format_display_temperature_imperial, temperatureMax);
            }
            holder.tvTemperature.setText(temperatureFormatted);
            holder.tvMinTemperature.setText(temperatureMinFormatted);
            holder.tvMaxTemperature.setText(temperatureMaxFormatted);

            // set the values in views in layoutContainer2 (dropdown from weather item display)
            holder.tvTimeHoursMins.setText(conversionUtils.getTimeHoursMinutesFromStamp(
                    mCursor.getInt(mCursor.getColumnIndex(DBContract.WeatherEntry.TIMESTAMP))));
            holder.tvTimeDay.setText(conversionUtils.getTimeDay(
                    mCursor.getInt(mCursor.getColumnIndex(DBContract.WeatherEntry.TIMESTAMP))));

            if (mCursor.isNull(mCursor.getColumnIndex(DBContract.WeatherEntry.RAIN)))
                holder.tvRain.setText(mContext.getString(R.string.format_display_rain, 0.0));
            else
                holder.tvRain.setText(mContext.getString(R.string.format_rain_snow_wind,
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.RAIN))));

            if(mCursor.isNull(mCursor.getColumnIndex(DBContract.WeatherEntry.SNOW)))
                holder.tvSnow.setText(mContext.getString(R.string.format_display_snow, 0.0));
            else
                holder.tvSnow.setText(mContext.getString(R.string.format_rain_snow_wind,
                        mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.SNOW))));

            if (mCursor.isNull(mCursor.getColumnIndex(DBContract.WeatherEntry.WIND_DEGREE)))
                holder.tvWind.setText(mContext.getString(R.string.format_display_wind, 0.0));
            else{
                Double windspeed = mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.WIND_SPEED));
                Double winddegree = mCursor.getDouble(mCursor.getColumnIndex(DBContract.WeatherEntry.WIND_DEGREE));
                // !!!!!!!!!!! WIND_DEGREE is not being printed right now . To be implemented later !!!!!!!!!!!!!!!!!!
                holder.tvWind.setText(mContext.getString(R.string.format_rain_snow_wind,windspeed) + mContext.getResources().getString(R.string.units_msec));
            }

            // hide the layoutContainerBottom section for views not in position 0
            if (holder.layoutConstraintBottom!=null)
                holder.layoutConstraintBottom.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor!=null)
            return mCursor.getCount();
        else
            return 0;
    }

    public void swapCursor(Cursor cur){

        if (cur!=mCursor){
            Log.d(TAG,"swapCursor(): called with new cursor data");
            mCursor = cur;
            this.notifyDataSetChanged();
        }else
            Log.d(TAG,"swapCursor(): called with same old cursor data");
    }

    public class WeatherDisplayViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemperature, tvMinTemperature, tvMaxTemperature,tvWeatherDescription,tvTimeHoursMins, tvTimeDay;
        TextView tvRain, tvWind, tvSnow;
        ImageView ivWeatherIcon;
        ConstraintLayout layoutConstraintTop, layoutConstraintBottom;
        ImageView btnDropDownController;

        public WeatherDisplayViewHolder(View viewholder){
            super(viewholder);

            ivWeatherIcon = (ImageView) viewholder.findViewById(R.id.ivWeatherIcon);

            tvTemperature = (TextView) viewholder.findViewById(R.id.tvTemperature);
            tvMinTemperature = (TextView) viewholder.findViewById(R.id.tvMinTemperature);
            tvMaxTemperature = (TextView) viewholder.findViewById(R.id.tvMaxTemperature);
            tvWeatherDescription = (TextView) viewholder.findViewById(R.id.tvWeatherDescription);
            tvTimeHoursMins = (TextView) viewholder.findViewById(R.id.tvTimeHoursMins);
            tvTimeDay = (TextView) viewholder.findViewById(R.id.tvTimeDay);
            tvRain = (TextView) viewholder.findViewById(R.id.tvRain);
            tvWind = (TextView) viewholder.findViewById(R.id.tvWind);
            tvSnow = (TextView) viewholder.findViewById(R.id.tvSnow);

            layoutConstraintTop = (ConstraintLayout) viewholder.findViewById(R.id.layoutContainerTop);
            layoutConstraintBottom = (ConstraintLayout) viewholder.findViewById(R.id.layoutContainerBottom);

            btnDropDownController = (ImageView) viewholder.findViewById(R.id.btnDropDownController);
            if (btnDropDownController!=null) {
                // setup button click action on drop-down controller
                btnDropDownController.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // if layoutBottom is not visible: make it visible, show drop-up button
                        if (layoutConstraintBottom.getVisibility() == View.GONE) {
                            Log.d(TAG, "onClick(): Show layoutConstraintBottom");
                            layoutConstraintBottom.setVisibility(View.VISIBLE);
                            btnDropDownController.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        } else {
                            // if layoutBottom is visible: then hide it, show drop-down button
                            Log.d(TAG, "onClick(): Hide layoutConstraintBottom");
                            layoutConstraintBottom.setVisibility(View.GONE);
                            btnDropDownController.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        }
                    }
                });
            }// the null-check accounts for the view in position 1
        }
    }
}

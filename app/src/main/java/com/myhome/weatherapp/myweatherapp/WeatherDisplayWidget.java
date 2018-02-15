package com.myhome.weatherapp.myweatherapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.myhome.weatherapp.myweatherapp.activities.WeatherActivity;
import com.myhome.weatherapp.myweatherapp.openweathermap.OpenWeatherUtils;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherDisplayWidget extends AppWidgetProvider {

    /*
    * Method to inflate individual widget instances on the home screen
    * Here, you can customize the type of layout to map to a given widgetId
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bundle args) {

        // Construct the RemoteViews object for the entire layout
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_display_widget);

        if (args != null) {
            // get the data from the passed in Bundle
            String cityName = args.getString(DBContract.CityList.CITY_NAME, null);
            String temperature = args.getString(DBContract.WeatherEntry.TEMPERATURE, null);
            String temperatureMin = args.getString(DBContract.WeatherEntry.TEMPERATURE_MIN, null);
            String temperatureMax = args.getString(DBContract.WeatherEntry.TEMPERATURE_MAX, null);
            String weatherIcon = args.getString(DBContract.WeatherEntry.WEATHER_ICON, null);
            String weatherDesc = args.getString(DBContract.WeatherEntry.WEATHER_DESC, null);
            String rain = args.getString(DBContract.WeatherEntry.RAIN, null);
            String wind = args.getString(DBContract.WeatherEntry.WIND_SPEED, null);
            String snow = args.getString(DBContract.WeatherEntry.SNOW, null);

            views.setTextViewText(R.id.tvWidgetLocationName, cityName);
            views.setTextViewText(R.id.tvWidgetTemperature, temperature);
            views.setTextViewText(R.id.tvWidgetMinTemperature, temperatureMin);
            views.setTextViewText(R.id.tvMaxTemperature, temperatureMax);
            views.setImageViewResource(R.id.ivWeatherIcon,
                    OpenWeatherUtils.getImageResourceIdentifier(context, weatherIcon));
            views.setTextViewText(R.id.tvWidgetWeatherDescription, weatherDesc);
            views.setTextViewText(R.id.tvWidgetRain, rain);
            views.setTextViewText(R.id.tvWidgetWind, wind);
            views.setTextViewText(R.id.tvWidgetSnow, snow);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                new Intent(context, WeatherActivity.class),
                0);
        views.setOnClickPendingIntent(R.id.layoutRoot, pendingIntent);
        views.setOnClickPendingIntent(R.id.ivWeatherIcon, pendingIntent);
        views.setOnClickPendingIntent(R.id.tvMaxTemperature, pendingIntent);
        views.setOnClickPendingIntent(R.id.tvMinTemperature, pendingIntent);
        views.setOnClickPendingIntent(R.id.tvTemperature, pendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }
    }

    // this method is called when a bcast. Intent is received by the widget
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle args = intent.getExtras();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int widgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherDisplayWidget.class));
        for (int appWidgetId : widgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, args);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


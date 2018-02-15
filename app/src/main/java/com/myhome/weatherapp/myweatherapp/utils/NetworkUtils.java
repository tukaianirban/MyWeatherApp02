package com.myhome.weatherapp.myweatherapp.utils;

import android.util.JsonReader;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by emuanir on 2/1/2018.
 */

public class NetworkUtils {

    static final String TAG = "DEBUG(NetworkUtils):";

    public static JsonReader getResponseFromURL(URL url) throws IOException{

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
            String recvText = IOUtils.toString(in);
            JsonReader reader = new JsonReader(new InputStreamReader(IOUtils.toInputStream(recvText)));

            return reader;
        }catch (Exception ex){
            Log.d(TAG,"getResponseFromURL(): encountered exception. Trace:"+ex.getMessage());
            return null;
        }finally{
            urlConnection.disconnect();
        }
    }
}

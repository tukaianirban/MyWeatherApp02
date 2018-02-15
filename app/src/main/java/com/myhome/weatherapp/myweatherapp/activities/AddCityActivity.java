package com.myhome.weatherapp.myweatherapp.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myhome.weatherapp.myweatherapp.R;
import com.myhome.weatherapp.myweatherapp.openweathermap.weatherContract;
import com.myhome.weatherapp.myweatherapp.provider.DBContract;
import com.myhome.weatherapp.myweatherapp.utilcomponents.CitySuggestiveListLoader;
import com.myhome.weatherapp.myweatherapp.utilcomponents.SelectedCitiesAdapter;
import com.myhome.weatherapp.myweatherapp.utilcomponents.SelectedCitiesTouchHelper;
import com.myhome.weatherapp.myweatherapp.utilcomponents.SelectedCitiesTouchInterface;
import com.myhome.weatherapp.myweatherapp.utilcomponents.ServiceRefreshWeatherData;

import java.util.List;
import java.util.Map;

public class AddCityActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks
        {

    private static final String TAG = "DEBUG(AddCity):";

    // member variables
    public String cityIds[], cityNames[], countryCodes[];
    private ArrayAdapter<String> mAdapterCityNames;
    private static final int LOADER_GET_SELECTED_CITY_LIST = 1024, LOADER_GET_SUGGESTIVE_CITY_LIST = 1025;

    // member views
    AutoCompleteTextView etAddNewCity;
    ImageButton btnAddNewCity;
    RecyclerView rvShowSelectedCities;
    ItemTouchHelper mItemTouchHelper;
    SelectedCitiesAdapter mAdapterSelectedCities;

    //member components
    Cursor mCursor;                             // cursor of selected-cities list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        // fire-off the asynctask to load the city ids/names/country-codes for use in suggestions for the "Add new city"
        getSupportLoaderManager().initLoader(LOADER_GET_SUGGESTIVE_CITY_LIST, null, this);

        // set up the autocomplete-edittext
        etAddNewCity = (AutoCompleteTextView) findViewById(R.id.etAddNewCity);

        // setup the button to add in a new city into the list
        btnAddNewCity = (ImageButton) findViewById(R.id.btnAddNewCity);
        fnSetupAddCityButtonClick();

        // setup the recyclerview for display of the cities
        rvShowSelectedCities = (RecyclerView) findViewById(R.id.rvShowCities);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvShowSelectedCities.setLayoutManager(layoutManager);

        mAdapterSelectedCities = new SelectedCitiesAdapter(this);
        rvShowSelectedCities.setAdapter(mAdapterSelectedCities);

        // create an instance of the SelectedCitiesTouchHelper as an ItemTouchHelper.Callback
        // use that to create an ItemTouchHelper, and then attach it to the recyclerView
        ItemTouchHelper.Callback callback = new SelectedCitiesTouchHelper(mAdapterSelectedCities);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(rvShowSelectedCities);

        // start the loader to load the selected cities list
        getSupportLoaderManager().initLoader(LOADER_GET_SELECTED_CITY_LIST, null, this);
    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_GET_SELECTED_CITY_LIST:
                Log.d(TAG,"onCreateLoader(): create a new cursorloader for Selected Cities list");
                return new CursorLoader(this, DBContract.CITY_CONTENT_URI, null, null, null, null);
            case LOADER_GET_SUGGESTIVE_CITY_LIST:
                Log.d(TAG, "onCreateLoader(): called for (Suggestive city list) loader id=" + id);
                return new CitySuggestiveListLoader(this, null);
            default:
                Log.w(TAG,"onCreateLoader(): called for unsupported loader id="+id);
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

        switch (loader.getId()){
            case LOADER_GET_SUGGESTIVE_CITY_LIST:
                Map<String, String[]> mapCityInfo = (Map<String, String[]>) data;

                cityNames = mapCityInfo.get(weatherContract.cityObject.CITY_NAME);
                cityIds = mapCityInfo.get(weatherContract.cityObject.CITY_ID);
                countryCodes = mapCityInfo.get(weatherContract.cityObject.COUNTRY_CODE);

                fnSetupAddCityAutoCompletion();
                Toast.makeText(this, "Autocompletion adapter loaded with new data", Toast.LENGTH_SHORT).show();
                break;
            case LOADER_GET_SELECTED_CITY_LIST:
                try {
                    // store the selected-cities list in a local cursor
                    mCursor = (Cursor)data;
                    if (mCursor==null)
                        Log.d(TAG,"onLoadFinished(): Selected city loader fetched a null cursor");
                    else{
                        Log.d(TAG,"onLoadFinished(): Selected city loader fetched a valid cursor");
                    }
                    mAdapterSelectedCities.swapCursor(mCursor);
                }catch (Exception ex){
                    Log.w(TAG,"onLoadFinished(): exception converting Loader data to Cursor object for Selected cities data. Trace:"+ex.getMessage());
                    ex.printStackTrace();
                }
                //smooth scroll the recyclerview to the end
                rvShowSelectedCities.smoothScrollToPosition(mAdapterSelectedCities.getItemCount());
                break;
            default:
                Log.i(TAG,"onLoadFinished(): unrecognized Loader data");
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, "onLoaderReset() called");
        switch(loader.getId()){
            case LOADER_GET_SELECTED_CITY_LIST:
                mAdapterSelectedCities.swapCursor(null);
                break;
            case LOADER_GET_SUGGESTIVE_CITY_LIST:
                mAdapterCityNames=null;
                break;
            default:
                Log.d(TAG,"onLoaderReset(): called on unknown loader");
        }
    }

    private void fnSetupAddCityAutoCompletion() {
        if (cityNames==null){
            Log.d(TAG,"fnSetupAddCityAutoCompletion(): no City names loaded yet. Cityname text auto-complete disabled");
            return;
        }
        mAdapterCityNames = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, cityNames);
        etAddNewCity.setAdapter(mAdapterCityNames);
        Log.d(TAG,"fnSetupAddCityAutoCompletion(): setting autocompleteTextView with adapter of length = "+ mAdapterCityNames.getCount());
        etAddNewCity.setThreshold(3);                   // auto-completion starts only after user types in atleast 3 characters

    }

    private void fnSetupAddCityButtonClick() {

        /** Run an AsyncTask background thread to insert the new city into the database.
         * If the insert is successful, call restartLoader on the Selected Cities Loader object
         */
        btnAddNewCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddNewCityAsyncTask().execute(etAddNewCity.getText().toString());

            }
        });
    }

            /**
             * AsyncTask class to add a new City into the user-selected list of cities
             * 1. Update user-selected city list in this activity
             * 2. Run ServiceRefreshWeatherData IntentService to reload info about the new ciy from OpenWeather server
             */
    public class AddNewCityAsyncTask extends AsyncTask<String, Void, Boolean>{

        private static final String TAG = "DEBUG(AddCityClick):";

        @Override
        protected Boolean doInBackground(String... enteredCityNames) {
            String tempCityName = enteredCityNames[0];
            if (cityNames==null || cityNames.length<=0) {
                Log.d(TAG, "doInBackground(): Suggestive cities list is null or empty");
                return false;
            }else{
                Log.d(TAG,"doInBackground(): Suggestive cities list has "
                        + cityNames.length + " records");
            }
            Log.d(TAG,"doInBackground(): Entered city="+tempCityName+" : searching now in database");

                        /*
                         *  check if the user-entered cityName exists in the Suggestive cities list
                         */
            ContentValues values = new ContentValues();
            boolean flagAddNewCity=false;
            for (int i=0;i<cityNames.length;i++){
                if (tempCityName.equals(cityNames[i])){
                    flagAddNewCity=true;

                    values.put(DBContract.CityList.CITY_NAME, cityNames[i]);
                    values.put(DBContract.CityList.ID, cityIds[i]);
                    values.put(DBContract.CityList.COUNTRY_CODE, countryCodes[i]);
                    break;
                }
            }

            if (!flagAddNewCity){
                Toast.makeText(AddCityActivity.this, "This city is not known to us yet.", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"doInBackground(): User-enterec city="+tempCityName+" does not match any suggestive-city list's city");
            }else {
                // check if the user-entered cityname is already present in the user-selected cities list
                mCursor.moveToFirst();
                while (mCursor.moveToNext()) {
                    String existingCityName = mCursor.getString(mCursor.getColumnIndex(DBContract.CityList.CITY_NAME));
                    if (existingCityName.equals(tempCityName)) {
                        Toast.makeText(AddCityActivity.this, tempCityName + " is already enlisted", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "User selected cityname is already in the Selected Cities list");
                        flagAddNewCity = false;
                    }
                }
            }

            if (flagAddNewCity==true){
                // the user-entered city is present in the suggestive cities list and
                // not present in the user-selected list
                Uri responseUri = getContentResolver().insert(DBContract.CITY_CONTENT_URI, values);
                if (responseUri!=null)
                    Log.d(TAG,"doInBackground(): inserted 1 city record into the database");
                //mAdapterCityNames.notifyDataSetChanged();
                return true;
            }else {
                Log.d(TAG, "doInBackground(): user-entered city cannot be added in");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean){
                            /*
                            1. Update the AdapterView (RecyclerView) on this activity to show the user-selected
                               list of cities
                            2. When insert happens in DB from this Loader, observers of CITY_CONTENT_URI is
                               already notified of changes (NO ACTION needed here)
                            3. Run the ServiceRefreshWeatherData IntentService to load up current weather for all
                               user-selected cities
                             */
                Log.d(TAG,"doInBackground(): restarting Loader to refresh the list of user-selected cities");
                getSupportLoaderManager().restartLoader(LOADER_GET_SELECTED_CITY_LIST, null, AddCityActivity.this);
                Log.d(TAG,"doInBackground(): run ServiceRefreshWeatherData IntentService to reload all weather data for all user-selected cities");
                startService(new Intent(AddCityActivity.this, ServiceRefreshWeatherData.class));
            }

            // close any drop-downs from AutoCompleteTextView, clear the text in it, close down the keyboard if its open
            etAddNewCity.dismissDropDown();
            etAddNewCity.setText("");
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(etAddNewCity.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }
}
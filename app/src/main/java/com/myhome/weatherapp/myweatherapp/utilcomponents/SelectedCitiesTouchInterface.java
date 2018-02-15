package com.myhome.weatherapp.myweatherapp.utilcomponents;

/**
 * Created by emuanir on 1/31/2018.
 */

public interface SelectedCitiesTouchInterface {

    /**
     * This method is invoked by the ItemTouchHelper class when an item is swiped right/left
     * @param position : the position of the item in the adapter
     */
    void onItemDismiss(int position);
}

package com.myhome.weatherapp.myweatherapp.utilcomponents;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by emuanir on 1/31/2018.
 */

public class SelectedCitiesTouchHelper extends ItemTouchHelper.Callback {

    private SelectedCitiesTouchInterface mCitiesTouchInterface;

    public SelectedCitiesTouchHelper(SelectedCitiesTouchInterface citiesTouchInterface){
        mCitiesTouchInterface = citiesTouchInterface;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * Set flags for the movements supported by the ItemTouchHelper
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        final int dragFlags = 0;

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        /**
         * The Recycler.ViewHolder is part of the RecyclerView, and therefore it can get its own position in the adapter
         * Send this "adapter position" to calls for ItemDismiss, so that all application components implementing
         * the call will be able to act in their own way on it
         */
        mCitiesTouchInterface.onItemDismiss(viewHolder.getAdapterPosition());
    }
}

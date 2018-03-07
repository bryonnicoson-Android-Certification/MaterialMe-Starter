/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.materialme;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

/***
 * Main Activity for the Material Me app, a mock sports news application with poor design choices
 */
public class MainActivity extends AppCompatActivity {

    //Member variables
    private RecyclerView mRecyclerView;
    private ArrayList<Sport> mSportsData;
    private SportsAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    //Save instance state
    public final static String LIST_STATE_KEY = "recycler_list_state";
    public final static String LIST_DATA_KEY = "sports_data";
    Parcelable listState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //get the column count from integers.xml or integers.xml (land)
        int gridColumnCount = getResources().getInteger(R.integer.grid_column_count);

        //Initialize the LayoutManager
        mLayoutManager = new GridLayoutManager(this, gridColumnCount);

        //Set the Layout Manager to the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);

        //Restore mSportsData ArrayList state and position or create new
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            mSportsData = savedInstanceState.getParcelableArrayList(LIST_DATA_KEY);
        } else {
            mSportsData = new ArrayList<>();
        }

        //Initialize the Adapter and set to the View
        mAdapter = new SportsAdapter(this, mSportsData);
        mRecyclerView.setAdapter(mAdapter);

        //if no data from saved state, initialize data
        if (savedInstanceState == null)
            initializeData();

        /**
         *  Optional logic to disable Swipe to Dismiss on landscape orientation
         *
         *  int swipeDirs;
         *  if(gridColumnCount > 1){
         *      swipeDirs = 0;
         *  } else {
         *      swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
         *  }
         *  ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallBack
         *      (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN
         *          | ItemTouchHelper.UP, swipeDirs)
         *
         */

        //Implement Swipe to Dismiss and Drag & Drop card behavior and attach to RecyclerView
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(mSportsData, from, to);
                mAdapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mSportsData.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        helper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * Method for initializing the sports data from resources.
     */
    private void initializeData() {
        //Get the resources from the XML file
        String[] sportsList = getResources().getStringArray(R.array.sports_titles);
        String[] sportsInfo = getResources().getStringArray(R.array.sports_info);
        TypedArray sportsImageResources = getResources().obtainTypedArray(R.array.sports_images);

        //Clear the existing data (to avoid duplication)
        mSportsData.clear();

        //Create the ArrayList of Sports objects with the titles and information about each sport
        for (int i = 0; i < sportsList.length; i++) {
            mSportsData.add(new Sport(sportsList[i], sportsInfo[i],
                    sportsImageResources.getResourceId(i, 0)));
        }

        // Now that we have an ArrayList of Sports objects, clean up data in typed array
        sportsImageResources.recycle();

        //Notify the adapter of the change
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Fab onClick method
     *
     * @param view - fab
     */
    public void resetSports(View view) {
        initializeData();
    }

    /**
     * methods to save and restore state of recyclerview layout onRestore and onResume
     *
     * @param outState - bundle of instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        listState = mLayoutManager.onSaveInstanceState();
        outState.putParcelableArrayList(LIST_DATA_KEY, mSportsData);
        outState.putParcelable(LIST_STATE_KEY, listState);
    }
}

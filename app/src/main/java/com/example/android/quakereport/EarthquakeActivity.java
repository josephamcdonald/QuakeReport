/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>> {

    private static final String LOG_TAG = EarthquakeActivity.class.getName();

    private static final int EARTHQUAKE_LOADER_ID = 0;

    /**
     * URL for earthquake data from the USGS dataset.
     */
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=5&limit=10";

    // Declare the earthquake adapter.
    private EarthquakeAdapter earthquakeAdapter;

    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find the ListView.
        ListView earthquakeListView = findViewById(R.id.list);

        // Find and set the empty TextView.
        emptyStateTextView = findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(emptyStateTextView);

        // Get the LoaderManager to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Check Internet connectivity.
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
            Log.i(LOG_TAG, "TEST: initLoader() called.");
        } else {

            // Find the progress indicator circle view and set the "No Internet" text.
            ProgressBar progressCircle = findViewById(R.id.progress_circle);
            progressCircle.setVisibility(View.GONE);
            emptyStateTextView.setText(R.string.no_internet);
        }

        // Set the earthquake adapter.
        earthquakeAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(earthquakeAdapter);

        // Create OnItemClickListener.
        AdapterView.OnItemClickListener myOnItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Find the current earthquake that was clicked on.
                Earthquake currentEarthquake = earthquakeAdapter.getItem(position);

                // Create intent to go to current earthquake URL.
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentEarthquake.getEqUrl()));
                startActivity(urlIntent);
            }
        };
        // Set OnItemClickListener.
        earthquakeListView.setOnItemClickListener(myOnItemClickListener);
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "TEST: onCreateLoader() called.");

        // Create and return a new loader for the given URL.
        return new EarthquakeLoader(this, USGS_REQUEST_URL);
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        Log.i(LOG_TAG, "TEST: onLoaderReset() called.");

        // Clear the adapter.
        earthquakeAdapter.clear();
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> data) {
        Log.i(LOG_TAG, "TEST: onLoadFinished() called.");

        ProgressBar progressCircle = findViewById(R.id.progress_circle);
        progressCircle.setVisibility(View.GONE);
        emptyStateTextView.setText(R.string.no_earthquakes);

        // Clear the adapter.
        earthquakeAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            earthquakeAdapter.addAll(data);
        }
    }
}

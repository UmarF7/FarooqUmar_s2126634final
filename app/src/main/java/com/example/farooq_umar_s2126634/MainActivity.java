/*
 * Name: Umar Farooq
 * Student ID: S2126634
 * Programme of Study: Software Development
 */

package com.example.farooq_umar_s2126634;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        TrafficListFragment.SelectionListener, TrafficDetailFragment.BackListener,
        TrafficMapFragment.MapBackListener {

    private Button incidentsButton;
    private Button roadworksButton;
    private Button roadSearchButton;
    private Button nearbyButton;
    private Button mapResultsButton;
    private EditText roadSearchInput;
    private EditText radiusInput;
    private TextView dataStatus;
    private TextView searchStatus;
    private TrafficViewModel viewModel;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final long UPDATE_INTERVAL = 60 * 60 * 1000; // One hour
    private boolean fetching = false;
    private String clientId = "";
    private String clientKey = "";
    private String roadworksUrl = "";

    private final Runnable hourlyUpdate = new Runnable() {
        @Override
        public void run() {
            refreshLiveData();
            uiHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        incidentsButton = findViewById(R.id.incidentsButton);
        roadworksButton = findViewById(R.id.roadworksButton);
        roadSearchButton = findViewById(R.id.roadSearchButton);
        nearbyButton = findViewById(R.id.nearbyButton);
        mapResultsButton = findViewById(R.id.mapResultsButton);
        roadSearchInput = findViewById(R.id.roadSearchInput);
        radiusInput = findViewById(R.id.radiusInput);
        dataStatus = findViewById(R.id.dataStatus);
        searchStatus = findViewById(R.id.searchStatus);

        incidentsButton.setOnClickListener(this);
        roadworksButton.setOnClickListener(this);
        roadSearchButton.setOnClickListener(this);
        nearbyButton.setOnClickListener(this);
        mapResultsButton.setOnClickListener(this);

        ViewModelProvider provider = new ViewModelProvider(this);
        viewModel = provider.get(TrafficViewModel.class);
        searchStatus.setText(viewModel.getSearchDescription());

        readCredentials();

        if (viewModel.hasData()) {
            updateStatus();
            if (savedInstanceState == null) {
                showList();
            }
        } else {
            dataStatus.setText("Checking saved Traffic Scotland data");
            loadStartingData();
        }
    }

    private boolean hasCredentials() {
        return !clientId.isEmpty() && !clientKey.isEmpty()
                && !clientId.equals("ID_HERE")
                && !clientKey.equals("KEY_HERE");
    }

    private boolean hasFeedConfiguration() {
        return hasCredentials() && !roadworksUrl.isEmpty()
                && !roadworksUrl.equals("ROADWORKS_URL_HERE");
    }

    private void readCredentials() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            if (info.metaData != null) {
                clientId = info.metaData.getString(
                        "com.example.farooq_umar_s2126634.TRAFFIC_CLIENT_ID", "");
                clientKey = info.metaData.getString(
                        "com.example.farooq_umar_s2126634.TRAFFIC_CLIENT_KEY", "");
                roadworksUrl = info.metaData.getString(
                        "com.example.farooq_umar_s2126634.TRAFFIC_ROADWORKS_URL", "");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TrafficData", "Could not read traffic credentials", e);
        }
    }

    private void loadStartingData() {
        // Use the last downloaded XML if it exists
        new Thread(new Runnable() {
            @Override
            public void run() {
                TrafficFeedService.Feeds saved = null;
                String savedStatus = "";
                try {
                    saved = TrafficFeedService.readCache(MainActivity.this);
                    if (saved != null) {
                        savedStatus = "CACHED LIVE DATA - saved "
                                + dateText(TrafficFeedService.cacheTime(MainActivity.this));
                    }
                } catch (Exception e) {
                    Log.e("TrafficData", "Saved XML could not be read", e);
                }

                if (saved == null && !hasCredentials()) {
                    try {
                        String incidentXml = TrafficFeedService.readResource(
                                MainActivity.this, R.raw.unplannedcontentexample);
                        String roadworksXml = TrafficFeedService.readResource(
                                MainActivity.this, R.raw.roadworkscontentexample);
                        saved = TrafficFeedService.parse(incidentXml, roadworksXml);
                        savedStatus = "SAMPLE DATA ONLY, not live traffic";
                    } catch (Exception e) {
                        Log.e("TrafficData", "Sample XML could not be parsed", e);
                    }
                }

                final TrafficFeedService.Feeds result = saved;
                final String label = savedStatus;
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (result != null) {
                            viewModel.setData(result.incidents, result.roadworks);
                            viewModel.setSourceMessage(label);
                            updateStatus();
                            if (getSupportFragmentManager().findFragmentById(
                                    R.id.fragmentHolder) == null) {
                                showList();
                            }
                        } else if (!hasCredentials()) {
                            dataStatus.setText("Traffic Scotland access is not configured");
                        } else if (!hasFeedConfiguration()) {
                            dataStatus.setText("Add the Current Roadworks Content URL to local.properties");
                        } else {
                            dataStatus.setText("Connecting to the official Traffic Scotland feeds");
                        }
                        if (hasFeedConfiguration()) {
                            refreshLiveData();
                        }
                    }
                });
            }
        }).start();
    }

    private void refreshLiveData() {
        if (!hasFeedConfiguration() || fetching) {
            return;
        }
        fetching = true;
        dataStatus.setText("Checking Traffic Scotland for updated incidents and roadworks");

        new Thread(new Runnable() {
            @Override
            public void run() {
                TrafficFeedService.Feeds fresh = null;
                String problem = "";
                try {
                    // Both feeds must succeed before replacing the displayed data.
                    String incidentXml = TrafficFeedService.download(
                            TrafficFeedService.INCIDENTS_URL, clientId, clientKey);
                    String roadworksXml = TrafficFeedService.download(
                            roadworksUrl, clientId, clientKey);
                    fresh = TrafficFeedService.parse(incidentXml, roadworksXml);
                    try {
                        TrafficFeedService.saveCache(MainActivity.this,
                                incidentXml, roadworksXml);
                    } catch (IOException saveError) {
                        Log.e("TrafficData", "Could not save latest XML for offline use", saveError);
                    }
                } catch (Exception e) {
                    problem = e.getMessage();
                    Log.e("TrafficData", "Could not update official data", e);
                }

                final TrafficFeedService.Feeds result = fresh;
                final String errorMessage = problem;
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        fetching = false;
                        if (result != null) {
                            viewModel.setData(result.incidents, result.roadworks);
                            viewModel.setSourceMessage("LIVE TRAFFIC - checked "
                                    + dateText(System.currentTimeMillis()));
                            updateStatus();
                            // Update the list, but do not interrupt details or the map.
                            if (getSupportFragmentManager().findFragmentById(
                                    R.id.fragmentHolder) instanceof TrafficListFragment) {
                                showList();
                            } else if (getSupportFragmentManager().findFragmentById(
                                    R.id.fragmentHolder) == null) {
                                showList();
                            }
                        } else {
                            if (viewModel.hasData()) {
                                if (!viewModel.getSourceMessage().startsWith("SAMPLE")) {
                                    viewModel.setSourceMessage("CACHED LIVE DATA - "
                                            + "could not refresh");
                                }
                                updateStatus();
                            } else {
                                dataStatus.setText("No live data. "
                                        + (errorMessage.isEmpty()
                                        ? "Check internet, credentials and both feed URLs"
                                        : errorMessage));
                            }
                            if (!errorMessage.isEmpty()) {
                                Log.w("TrafficData", "Update failed: " + errorMessage);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private String dateText(long time) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(time));
    }

    private void updateStatus() {
        dataStatus.setText(viewModel.getSourceMessage() + "\n"
                + "Incidents: " + viewModel.getIncidents().size()
                + "     Roadworks: " + viewModel.getRoadworks().size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.removeCallbacks(hourlyUpdate);
        uiHandler.postDelayed(hourlyUpdate, UPDATE_INTERVAL);
        if (viewModel != null && viewModel.hasData() && hasFeedConfiguration()) {
            refreshLiveData();
        }
    }

    @Override
    protected void onPause() {
        uiHandler.removeCallbacks(hourlyUpdate);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        if (!viewModel.hasData()) {
            searchStatus.setText("Traffic data is still loading.");
            return;
        }

        if (view == mapResultsButton) {
            showMap(false);
            return;
        }

        if (view == incidentsButton) {
            viewModel.setShowRoadworks(false);
        } else if (view == roadworksButton) {
            viewModel.setShowRoadworks(true);
        } else if (view == roadSearchButton) {
            String road = roadSearchInput.getText().toString().trim();
            if (road.isEmpty()) {
                searchStatus.setText("Enter a road name, for example M8");
                return;
            }
            viewModel.searchByRoad(road);
        } else if (view == nearbyButton) {
            String radiusText = radiusInput.getText().toString().trim();
            try {
                double radius = Double.parseDouble(radiusText);
                if (radius <= 0) {
                    searchStatus.setText("Enter a distance greater than zero");
                    return;
                }
                viewModel.searchNearby(radius);
            } catch (NumberFormatException e) {
                searchStatus.setText("Enter a distance in kilometres");
                return;
            }
        }
        searchStatus.setText(viewModel.getSearchDescription());
        showList();
    }

    public void showList() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentHolder, new TrafficListFragment());
        transaction.commit();
    }

    @Override
    public void onSituationSelected(int position) {
        viewModel.selectSituation(position);
        showDetails();
    }

    private void showDetails() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentHolder, new TrafficDetailFragment());
        transaction.commit();
    }

    private void showMap(boolean selectedOnly) {
        TrafficMapFragment mapFragment = new TrafficMapFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean("selectedOnly", selectedOnly);
        mapFragment.setArguments(arguments);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentHolder, mapFragment);
        transaction.commit();
    }

    @Override
    public void onMapRequested() {
        showMap(true);
    }

    @Override
    public void onBackFromMap(boolean selectedOnly) {
        if (selectedOnly) {
            showDetails();
        } else {
            showList();
        }
    }

    @Override
    public void onBackToList() {
        showList();
    }

}

package com.example.farooq_umar_s2126634;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class TrafficMapFragment extends Fragment implements OnMapReadyCallback,
        View.OnClickListener {

    public interface MapBackListener {
        void onBackFromMap(boolean selectedOnly);
    }

    private MapBackListener listener;
    private TextView mapStatus;
    private boolean selectedOnly = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (MapBackListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traffic_map, container, false);
        Button backButton = view.findViewById(R.id.mapBackButton);
        mapStatus = view.findViewById(R.id.mapStatus);
        backButton.setOnClickListener(this);

        Bundle arguments = getArguments();
        if (arguments != null) {
            selectedOnly = arguments.getBoolean("selectedOnly", false);
        }

        if (selectedOnly) {
            mapStatus.setText("Selected traffic situation");
            backButton.setText("Back to details");
        } else {
            mapStatus.setText("Map of the current list or search results");
            backButton.setText("Back to results");
        }

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.googleMapHolder, mapFragment).commit();
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        TrafficViewModel viewModel = new ViewModelProvider(getActivity())
                .get(TrafficViewModel.class);
        ArrayList<TrafficSituation> situations = new ArrayList<>();

        if (selectedOnly) {
            TrafficSituation selected = viewModel.getSelectedSituation();
            if (selected != null) {
                situations.add(selected);
            }
        } else {
            situations = viewModel.getVisibleSituations();
        }

        int markerCount = 0;
        LatLng firstPosition = null;
        double lowestLatitude = 90;
        double highestLatitude = -90;
        double lowestLongitude = 180;
        double highestLongitude = -180;

        for (TrafficSituation item : situations) {
            String latitude = item.toLatitude;
            String longitude = item.toLongitude;
            if (latitude.isEmpty() || longitude.isEmpty()) {
                latitude = item.fromLatitude;
                longitude = item.fromLongitude;
            }
            if (latitude.isEmpty() || longitude.isEmpty()) {
                continue;
            }

            try {
                double lat = Double.parseDouble(latitude);
                double lon = Double.parseDouble(longitude);
                if (Double.isNaN(lat) || Double.isNaN(lon)
                        || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    continue;
                }

                LatLng position = new LatLng(lat, lon);
                String markerTitle = item.comment;
                if (markerTitle.isEmpty()) {
                    markerTitle = item.type;
                }
                googleMap.addMarker(new MarkerOptions().position(position)
                        .title(markerTitle));
                if (firstPosition == null) {
                    firstPosition = position;
                }
                if (lat < lowestLatitude) {
                    lowestLatitude = lat;
                }
                if (lat > highestLatitude) {
                    highestLatitude = lat;
                }
                if (lon < lowestLongitude) {
                    lowestLongitude = lon;
                }
                if (lon > highestLongitude) {
                    highestLongitude = lon;
                }
                markerCount++;
            } catch (NumberFormatException e) {
                // Skip records without usable coordinates.
            }
        }

        mapStatus.setText("Showing " + markerCount + " of "
                + situations.size() + " situations on the map");

        if (markerCount == 1) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 11));
        } else if (markerCount > 1) {
            // Centre the map on the area covered by the results.
            double middleLatitude = (lowestLatitude + highestLatitude) / 2;
            double middleLongitude = (lowestLongitude + highestLongitude) / 2;
            double latitudeRange = highestLatitude - lowestLatitude;
            double longitudeRange = highestLongitude - lowestLongitude;
            float zoom = 10;
            if (latitudeRange > 2 || longitudeRange > 2) {
                zoom = 5;
            } else if (latitudeRange > 1 || longitudeRange > 1) {
                zoom = 7;
            } else if (latitudeRange > 0.3 || longitudeRange > 0.3) {
                zoom = 8;
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(middleLatitude, middleLongitude), zoom));
        }
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onClick(View view) {
        listener.onBackFromMap(selectedOnly);
    }
}

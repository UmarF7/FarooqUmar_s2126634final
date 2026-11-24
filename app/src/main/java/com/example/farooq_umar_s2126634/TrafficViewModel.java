package com.example.farooq_umar_s2126634;

import android.location.Location;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;

public class TrafficViewModel extends ViewModel {
    private ArrayList<TrafficSituation> incidents = new ArrayList<>();
    private ArrayList<TrafficSituation> roadworks = new ArrayList<>();
    private ArrayList<TrafficSituation> searchResults = new ArrayList<>();
    private TrafficSituation selectedSituation;
    private boolean showRoadworks = false;
    private boolean searchActive = false;
    private boolean dataLoaded = false;
    private String searchDescription = "Select a list or search both feeds.";
    private String searchHeading = "Search results";
    private String sourceMessage = "";
    private String lastRoadQuery = "";
    private double lastRadius = 0;
    private boolean lastSearchWasRoad = true;

    public void setData(ArrayList<TrafficSituation> incidentList,
                        ArrayList<TrafficSituation> roadworkList) {
        incidents = incidentList;
        roadworks = roadworkList;
        dataLoaded = true;
        if (searchActive) {
            if (lastSearchWasRoad) {
                searchByRoad(lastRoadQuery);
            } else {
                searchNearby(lastRadius);
            }
        }
        if (selectedSituation != null) {
            TrafficSituation newSelection = findById(incidents, selectedSituation);
            if (newSelection == null) {
                newSelection = findById(roadworks, selectedSituation);
            }
            if (newSelection != null) {
                selectedSituation = newSelection;
            }
        }
    }

    private TrafficSituation findById(ArrayList<TrafficSituation> list,
                                      TrafficSituation oldSelection) {
        for (TrafficSituation item : list) {
            if (item.id.equals(oldSelection.id)
                    && item.category.equals(oldSelection.category)) {
                return item;
            }
        }
        return null;
    }

    public void setSourceMessage(String message) {
        sourceMessage = message;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public boolean hasData() {
        return dataLoaded;
    }

    public ArrayList<TrafficSituation> getIncidents() {
        return incidents;
    }

    public ArrayList<TrafficSituation> getRoadworks() {
        return roadworks;
    }

    public void setShowRoadworks(boolean value) {
        showRoadworks = value;
        searchActive = false;
        if (value) {
            searchDescription = "Showing roadworks.";
        } else {
            searchDescription = "Showing incidents.";
        }
    }

    public boolean isShowingRoadworks() {
        return showRoadworks;
    }

    public boolean isSearchActive() {
        return searchActive;
    }

    public String getSearchHeading() {
        return searchHeading;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public ArrayList<TrafficSituation> getVisibleSituations() {
        if (searchActive) {
            return searchResults;
        }
        if (showRoadworks) {
            return roadworks;
        }
        return incidents;
    }

    public void searchByRoad(String road) {
        searchResults = new ArrayList<>();
        lastSearchWasRoad = true;
        lastRoadQuery = road;
        String searchText = road.toLowerCase();

        // Search incidents and roadworks, not only the current list.
        for (TrafficSituation item : incidents) {
            if (item.roadName.toLowerCase().contains(searchText)
                    || item.comment.toLowerCase().contains(searchText)) {
                searchResults.add(item);
            }
        }
        for (TrafficSituation item : roadworks) {
            if (item.roadName.toLowerCase().contains(searchText)
                    || item.comment.toLowerCase().contains(searchText)) {
                searchResults.add(item);
            }
        }
        searchActive = true;
        searchHeading = "Road: " + road;
        searchDescription = "Road search: " + road + " (both feeds)";
    }

    public void searchNearby(double radiusKm) {
        searchResults = new ArrayList<>();
        lastSearchWasRoad = false;
        lastRadius = radiusKm;

        // Glasgow city centre, used as the fixed reference point.
        double centreLatitude = 55.8642;
        double centreLongitude = -4.2518;

        for (TrafficSituation item : incidents) {
            if (withinDistance(item, centreLatitude, centreLongitude, radiusKm)) {
                searchResults.add(item);
            }
        }
        for (TrafficSituation item : roadworks) {
            if (withinDistance(item, centreLatitude, centreLongitude, radiusKm)) {
                searchResults.add(item);
            }
        }
        searchActive = true;
        searchHeading = "Within " + radiusKm + " km of Glasgow";
        searchDescription = "Within " + radiusKm + " km of Glasgow centre (both feeds)";
    }

    private boolean withinDistance(TrafficSituation item, double centreLatitude,
                                   double centreLongitude, double radiusKm) {
        if (pointWithinRadius(item.toLatitude, item.toLongitude,
                centreLatitude, centreLongitude, radiusKm)) {
            return true;
        }
        return pointWithinRadius(item.fromLatitude, item.fromLongitude,
                centreLatitude, centreLongitude, radiusKm);
    }

    private boolean pointWithinRadius(String latitude, String longitude,
                                      double centreLatitude, double centreLongitude,
                                      double radiusKm) {
        if (latitude.isEmpty() || longitude.isEmpty()) {
            return false;
        }
        try {
            double pointLatitude = Double.parseDouble(latitude);
            double pointLongitude = Double.parseDouble(longitude);
            float[] distance = new float[1];
            Location.distanceBetween(centreLatitude, centreLongitude,
                    pointLatitude, pointLongitude, distance);
            return distance[0] <= radiusKm * 1000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void selectSituation(int position) {
        selectedSituation = getVisibleSituations().get(position);
    }

    public TrafficSituation getSelectedSituation() {
        return selectedSituation;
    }
}

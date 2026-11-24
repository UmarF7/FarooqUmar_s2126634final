package com.example.farooq_umar_s2126634;

public class TrafficSituation {
    String category = "";
    String id = "";
    String type = "";
    String detailType = "";
    String roadName = "";
    String direction = "";
    String comment = "";
    String startTime = "";
    String endTime = "";
    String delay = "";
    String toLatitude = "";
    String toLongitude = "";
    String fromLatitude = "";
    String fromLongitude = "";

    public TrafficSituation(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        String result = type + "\n" + comment;
        result += "\nStart: " + startTime;
        if (!delay.isEmpty()) {
            result += "\nDelay: " + delay;
        }
        result += "\nTo: " + toLatitude + ", " + toLongitude + "\n";
        return result;
    }
}

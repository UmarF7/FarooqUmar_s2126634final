package com.example.farooq_umar_s2126634;

public class TrafficDisplay {

    public static int getIcon(TrafficSituation item) {
        if (item.type.equals("MaintenanceWorks")) {
            if (item.delay.equals("veryLongDelays")) {
                return R.drawable.ic_very_long_delay;
            } else if (item.delay.equals("longDelays")) {
                return R.drawable.ic_long_delay;
            } else if (item.delay.equals("delays")) {
                return R.drawable.ic_delay;
            }
            return R.drawable.ic_roadworks;
        } else if (item.type.equals("RoadOrCarriagewayOrLaneManagement")) {
            if (item.detailType.equals("roadClosed")) {
                return R.drawable.ic_road_closed;
            }
            return R.drawable.ic_other_traffic;
        } else if (item.type.equals("AbnormalTraffic")) {
            return R.drawable.ic_queue;
        } else if (item.type.equals("VehicleObstruction")) {
            if (item.detailType.equals("brokenDownVehicle")) {
                return R.drawable.ic_breakdown;
            }
            return R.drawable.ic_obstruction;
        } else if (item.type.equals("GeneralObstruction")) {
            return R.drawable.ic_obstruction;
        }
        return R.drawable.ic_other_traffic;
    }

    public static String getLabel(TrafficSituation item) {
        if (item.type.equals("MaintenanceWorks")) {
            if (item.delay.equals("veryLongDelays")) {
                return "Very long delays reported";
            } else if (item.delay.equals("longDelays")) {
                return "Long delays reported";
            } else if (item.delay.equals("delays")) {
                return "Delays reported";
            }
            return "Delay not specified";
        } else if (item.type.equals("RoadOrCarriagewayOrLaneManagement")) {
            if (item.detailType.equals("roadClosed")) {
                return "Road closure";
            }
            return "Road or lane management";
        } else if (item.type.equals("AbnormalTraffic")) {
            if (item.detailType.equals("queuingTraffic")) {
                return "Queuing traffic";
            }
            return "Abnormal traffic";
        } else if (item.type.equals("VehicleObstruction")) {
            if (item.detailType.equals("brokenDownVehicle")) {
                return "Broken-down vehicle";
            }
            return "Vehicle obstruction";
        } else if (item.type.equals("GeneralObstruction")) {
            return "Obstruction on road";
        }
        return "Traffic situation";
    }

    public static int getLabelColor(TrafficSituation item) {
        if (item.type.equals("MaintenanceWorks")) {
            if (item.delay.equals("veryLongDelays")) {
                return R.color.traffic_severe;
            } else if (item.delay.equals("longDelays")) {
                return R.color.traffic_long;
            } else if (item.delay.equals("delays")) {
                return R.color.traffic_delay;
            }
        }
        return R.color.traffic_neutral;
    }
}

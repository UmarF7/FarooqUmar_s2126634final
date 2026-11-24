package com.example.farooq_umar_s2126634;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class TrafficFeedService {

    public static final String INCIDENTS_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";

    // Current Roadworks URL is read from the approved Traffic Scotland portal


    private static final String INCIDENT_CACHE = "last_incidents.xml";
    private static final String ROADWORK_CACHE = "last_roadworks.xml";

    public static class Feeds {
        public final ArrayList<TrafficSituation> incidents;
        public final ArrayList<TrafficSituation> roadworks;

        public Feeds(ArrayList<TrafficSituation> incidentList,
                     ArrayList<TrafficSituation> roadworkList) {
            incidents = incidentList;
            roadworks = roadworkList;
        }
    }

    public static String download(String address, String id, String key) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);


        String credentials = id + ":" + key;
        String encoded = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", encoded);

        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Traffic Scotland returned HTTP " + status);
            }
            try (InputStream input = connection.getInputStream()) {
                return readText(input);
            }
        } finally {
            connection.disconnect();
        }
    }

    public static Feeds parse(String incidentXml, String roadworksXml)
            throws IOException, XmlPullParserException {
        ArrayList<TrafficSituation> incidents =
                TrafficXmlParser.parseData(incidentXml, "Incident");
        ArrayList<TrafficSituation> roadworks =
                TrafficXmlParser.parseData(roadworksXml, "Roadworks");
        return new Feeds(incidents, roadworks);
    }

    public static Feeds readCache(Context context)
            throws IOException, XmlPullParserException {
        File incidents = new File(context.getFilesDir(), INCIDENT_CACHE);
        File roadworks = new File(context.getFilesDir(), ROADWORK_CACHE);
        if (!incidents.exists() || !roadworks.exists()) {
            return null;
        }
        String incidentXml;
        String roadworksXml;
        try (FileInputStream incidentInput = new FileInputStream(incidents);
             FileInputStream roadworkInput = new FileInputStream(roadworks)) {
            incidentXml = readText(incidentInput);
            roadworksXml = readText(roadworkInput);
        }
        return parse(incidentXml, roadworksXml);
    }

    public static long cacheTime(Context context) {
        File incidents = new File(context.getFilesDir(), INCIDENT_CACHE);
        File roadworks = new File(context.getFilesDir(), ROADWORK_CACHE);
        return Math.min(incidents.lastModified(), roadworks.lastModified());
    }

    public static void saveCache(Context context, String incidentXml, String roadworksXml)
            throws IOException {
        File incidents = new File(context.getFilesDir(), INCIDENT_CACHE);
        File roadworks = new File(context.getFilesDir(), ROADWORK_CACHE);
        try (FileOutputStream incidentOutput = new FileOutputStream(incidents);
             FileOutputStream roadworkOutput = new FileOutputStream(roadworks)) {
            incidentOutput.write(incidentXml.getBytes(StandardCharsets.UTF_8));
            roadworkOutput.write(roadworksXml.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String readResource(Context context, int resourceId) throws IOException {
        try (InputStream input = context.getResources().openRawResource(resourceId)) {
            return readText(input);
        }
    }

    private static String readText(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder xml = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            xml.append(line).append('\n');
        }
        return xml.toString();
    }
}

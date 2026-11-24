package com.example.farooq_umar_s2126634;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class TrafficXmlParser {

    public static ArrayList<TrafficSituation> parseData(String xml, String category)
            throws XmlPullParserException, IOException {
        ArrayList<TrafficSituation> situations = new ArrayList<>();
        TrafficSituation current = null;
        boolean insideComment = false;
        boolean insideTo = false;
        boolean insideFrom = false;
        boolean insideLocationName = false;
        boolean insideIlc = false;
        boolean roadFromIlc = false;
        boolean foundPublication = false;

        {
            // Set up the XML parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            // Ignore any stray characters before the XML declaration.
            int xmlStart = xml.indexOf("<?xml");
            if (xmlStart > 0) {
                xml = xml.substring(xmlStart);
            }
            xpp.setInput(new StringReader(xml));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();

                    if (tag.equalsIgnoreCase("payloadPublication")) {
                        foundPublication = true;
                    } else if (tag.equalsIgnoreCase("situationRecord")) {
                        current = new TrafficSituation(category);
                        current.type = xpp.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance", "type");
                        current.id = xpp.getAttributeValue(null, "id");
                        if (current.type == null) { current.type = ""; }
                        if (current.id == null) { current.id = ""; }
                        roadFromIlc = false;
                    } else if (current != null) {
                        if (tag.equalsIgnoreCase("generalPublicComment")) {
                            insideComment = true;
                        } else if (tag.equalsIgnoreCase("to")) {
                            insideTo = true;
                        } else if (tag.equalsIgnoreCase("from")) {
                            insideFrom = true;
                        } else if (tag.equalsIgnoreCase("name") && (insideTo || insideFrom)) {
                            insideLocationName = true;
                        } else if (tag.equalsIgnoreCase("ilc") && (insideTo || insideFrom)) {
                            insideIlc = true;
                        } else if (tag.equalsIgnoreCase("value") && insideComment) {
                            String value = xpp.nextText();
                            if (current.comment.isEmpty()) {
                                current.comment = value;
                            }
                        } else if (tag.equalsIgnoreCase("value") && insideIlc && !roadFromIlc) {
                            // Junction records can give the road name inside an ilc tag.
                            current.roadName = xpp.nextText();
                            roadFromIlc = true;
                        } else if (tag.equalsIgnoreCase("value") && insideLocationName
                                && current.roadName.isEmpty()) {
                            current.roadName = xpp.nextText();
                        } else if (tag.equalsIgnoreCase("tpegDirection")) {
                            current.direction = xpp.nextText();
                        } else if (tag.equalsIgnoreCase("overallStartTime")) {
                            current.startTime = xpp.nextText();
                        } else if (tag.equalsIgnoreCase("overallEndTime")) {
                            current.endTime = xpp.nextText();
                        } else if (tag.equalsIgnoreCase("delaysType")) {
                            current.delay = xpp.nextText();
                        } else if (tag.equalsIgnoreCase("roadMaintenanceType")
                                || tag.equalsIgnoreCase("abnormalTrafficType")
                                || tag.equalsIgnoreCase("roadOrCarriagewayOrLaneManagementType")) {
                            current.detailType = xpp.nextText();
                        } else if (tag.equalsIgnoreCase("latitude")) {
                            String latitude = xpp.nextText();
                            if (insideTo) {
                                current.toLatitude = latitude;
                            } else if (insideFrom) {
                                current.fromLatitude = latitude;
                            }
                        } else if (tag.equalsIgnoreCase("longitude")) {
                            String longitude = xpp.nextText();
                            if (insideTo) {
                                current.toLongitude = longitude;
                            } else if (insideFrom) {
                                current.fromLongitude = longitude;
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String tag = xpp.getName();
                    if (tag.equalsIgnoreCase("situationRecord") && current != null) {
                        // Add the completed record to the list
                        situations.add(current);
                        current = null;
                        insideComment = false;
                        insideTo = false;
                        insideFrom = false;
                        insideLocationName = false;
                        insideIlc = false;
                    } else if (tag.equalsIgnoreCase("generalPublicComment")) {
                        insideComment = false;
                    } else if (tag.equalsIgnoreCase("name")) {
                        insideLocationName = false;
                    } else if (tag.equalsIgnoreCase("ilc")) {
                        insideIlc = false;
                    } else if (tag.equalsIgnoreCase("to")) {
                        insideTo = false;
                    } else if (tag.equalsIgnoreCase("from")) {
                        insideFrom = false;
                    }
                }
                eventType = xpp.next();
            }
        }
        if (!foundPublication) {
            throw new XmlPullParserException("No DATEX publication in the response");
        }
        return situations;
    }
}

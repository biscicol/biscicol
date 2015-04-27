package Render;

import BSCcore.BSCObject;
import util.geo;

import java.util.ArrayList;


/**
 * Render KML Output for use with Google Maps or Google Earth
 * @author Jdeck
 */
public class KMLRenderer extends TextRenderer {
    private String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n";
    private String footer = "</kml>\n";
    ArrayList geoList = new ArrayList();
    String geoString = "";
    boolean duplicate = false;

    public KMLRenderer() {
        super();
        setHeader(header);
        setFooter(footer);
    }

    public KMLRenderer(int maxdepth) {
        super(maxdepth);
        setHeader(header);
        setFooter(footer);
    }

    @Override
    protected String printEnterObject(BSCObject node, int depth, int child_cnt) {
        String placemark = "";
        duplicate = false;
        String type = node.getType();
        if (type.equalsIgnoreCase("geo:SpatialThing")) {
            geo g = new geo(node.toString());
            placemark += "<Placemark>\n";
            String parent = node.getParent().toString();
            placemark += "    <name>" + parent + "</name>\n";
            placemark += "    <description>Location referenced by: " +
                    "<br/>'" + parent + " relatedTo " + node.toString() + "'";

            Integer mem = g.getErrorradiusinmeters();
            if (mem != null) {
                placemark += "<br/>maxerrorinmeters = " + mem;
            }

            placemark += "</description>\n";
            try {


                geoString = node.toString();

                placemark += "    " + g.getKMLPointCoordinates() + "\n";
            } catch (Exception e) {
                placemark += "";
            }

        }
        // don't return duplicates
        if (geoList.contains(geoString)) {
            duplicate = true;
            return "";
        } else {
            geoList.add(geoString);
            duplicate = false;
            return placemark;
        }
    }

    @Override
    protected String printLeaveObject(BSCObject node, int depth, int child_cnt) {
        String res = "";

        String type = node.getType();
        // don't return duplicates
        if (duplicate) {
            return "";
        } else {
            if (type.equalsIgnoreCase("geo:SpatialThing")) {
                res = "</Placemark>\n";
            }
            return res;
        }
    }
}

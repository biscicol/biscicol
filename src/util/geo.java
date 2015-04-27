package util;

/**
 * Simple class to parse SpatialThing formatted identifiers into lat/lng/errorradius
 * Needs additional work to make this more robust and parse additional fields
 * User: biocode
 * Date: Aug 23, 2011
 * Time: 5:44:16 PM
 */
public class geo {
    private float latitude;
    private float longitude;
    private Integer errorradiusinmeters;

    public geo(String input) {

        // Everything after colon is data
        String data = input.split(":")[1];

        String[] elements = data.split(";");

        errorradiusinmeters = Integer.parseInt(elements[1].split("=")[1]);

        // lat/lng
        String[] latlng = elements[0].split(",");
        latitude = Float.parseFloat(latlng[0]);
        longitude = Float.parseFloat(latlng[1]);
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public Integer getErrorradiusinmeters() {
        return errorradiusinmeters;
    }

    public String getKMLPointCoordinates() {
        String kml = "";
        // long/lat/place at ground level
        kml += "<Point><coordinates>" + getLongitude() + "," + getLatitude() + ",0</coordinates></Point>";
        return kml;
    }


}

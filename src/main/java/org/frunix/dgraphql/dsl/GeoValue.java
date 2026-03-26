package org.frunix.dgraphql.dsl;

public record GeoValue(String type, Object value) implements DqlElement {

    public static GeoValue point(double lat, double lon) {
        return new GeoValue("Point", "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}");
    }

    public static GeoValue polygon(String coordinates) {
        return new GeoValue("Polygon", "{\"type\":\"Polygon\",\"coordinates\":" + coordinates + "}");
    }

    public static GeoValue multiPolygon(String coordinates) {
        return new GeoValue("MultiPolygon", "{\"type\":\"MultiPolygon\",\"coordinates\":" + coordinates + "}");
    }

    public static GeoValue lineString(String coordinates) {
        return new GeoValue("LineString", "{\"type\":\"LineString\",\"coordinates\":" + coordinates + "}");
    }

    public static GeoValue circle(double lat, double lon, double radiusKm) {
        return new GeoValue("Circle", "{\"type\":\"Circle\",\"coordinates\":[" + lon + "," + lat + "],\"radius\":" + radiusKm + "}");
    }

    @Override
    public String dql() {
        String s = (String) value;
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

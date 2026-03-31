package com.github.elfrucool.dgraphql.dsl;

/**
 * A geo value for geo queries.
 *
 * <p>Used with geo functions like {@code near}, {@code within}, {@code contains}, {@code intersects}:</p>
 *
 * <ul>
 *   <li>{@link #point(double, double)} - A geographic point</li>
 *   <li>{@link #polygon(String)} - A polygon area</li>
 *   <li>{@link #multiPolygon(String)} - Multiple polygons</li>
 *   <li>{@link #lineString(String)} - A line</li>
 *   <li>{@link #circle(double, double, double)} - A circular area</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * GeoValue.point(37.7749, -122.4194)  // San Francisco
 * GeoValue.circle(37.7749, -122.4194, 10)  // 10km radius
 * </pre>
 *
 * @see Func#near(String, GeoValue)
 * @see Func#within(String, GeoValue)
 */
public record GeoValue(String type, Object value) implements DqlElement {

    /**
     * Creates a geo point.
     *
     * <p>Example: {@code GeoValue.point(37.7749, -122.4194)}</p>
     *
     * @param lat Latitude
     * @param lon Longitude
     */
    public static GeoValue point(double lat, double lon) {
        return new GeoValue("Point", "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}");
    }

    /**
     * Creates a geo polygon.
     *
     * <p>Example: {@code GeoValue.polygon("[[[-122.4,37.7],[-122.4,37.8],[-122.3,37.8],[-122.3,37.7],[-122.4,37.7]]]")}</p>
     *
     * @param coordinates GeoJSON coordinates array
     */
    public static GeoValue polygon(String coordinates) {
        return new GeoValue("Polygon", "{\"type\":\"Polygon\",\"coordinates\":" + coordinates + "}");
    }

    /**
     * Creates a multi-polygon.
     *
     * @param coordinates GeoJSON coordinates array
     */
    public static GeoValue multiPolygon(String coordinates) {
        return new GeoValue("MultiPolygon", "{\"type\":\"MultiPolygon\",\"coordinates\":" + coordinates + "}");
    }

    /**
     * Creates a line string.
     *
     * @param coordinates GeoJSON coordinates array
     */
    public static GeoValue lineString(String coordinates) {
        return new GeoValue("LineString", "{\"type\":\"LineString\",\"coordinates\":" + coordinates + "}");
    }

    /**
     * Creates a circular area.
     *
     * <p>Example: {@code GeoValue.circle(37.7749, -122.4194, 10)}</p>
     *
     * @param lat Latitude of center
     * @param lon Longitude of center
     * @param radiusKm Radius in kilometers
     */
    public static GeoValue circle(double lat, double lon, double radiusKm) {
        return new GeoValue("Circle", "{\"type\":\"Circle\",\"coordinates\":[" + lon + "," + lat + "],\"radius\":" + radiusKm + "}");
    }

    @Override
    public String dql() {
        String s = (String) value;
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

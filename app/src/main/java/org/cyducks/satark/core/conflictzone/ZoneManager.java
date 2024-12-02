package org.cyducks.satark.core.conflictzone;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ZoneManager {
    private static final String TAG = "ZoneManager";

    public List<LatLng> getRedZone() {
        return redZone;
    }

    public void setRedZone(List<LatLng> redZone) {
        this.redZone = redZone;
    }

    public List<LatLng> getYellowZone() {
        return yellowZone;
    }

    public void setYellowZone(List<LatLng> yellowZone) {
        this.yellowZone = yellowZone;
    }

    public List<LatLng> getOrangeZone() {
        return orangeZone;
    }

    public void setOrangeZone(List<LatLng> orangeZone) {
        this.orangeZone = orangeZone;
    }

    private static final class Colors {
        static final int RED_ZONE = Color.argb(128, 255, 0, 0);
        static final int ORANGE_ZONE = Color.argb(128, 255,165,0);
        static final int YELLOW_ZONE = Color.argb(128, 255,255, 0);
        static final int STROKE = Color.BLACK;
    }

    private static final class Constants {
        static final double ORANGE_BUFFER_LiMIT = 200.0;
        static final double YELLOW_BUFFER_LIMIT = 600.0;
        static final double METERS_PER_LAT = 110947.2;
        static final double METERS_PER_LNG = 102470.9;
    }

    private final GoogleMap googleMap;
    private final List<Polygon> previewPolygons = new CopyOnWriteArrayList<>();

    private List<LatLng> redZone;
    private List<LatLng> yellowZone;
    private List<LatLng> orangeZone;

    public ZoneManager(GoogleMap map) {
        googleMap = map;
    }

    public void showZonePreviews(List<Polygon> selectedPolygons) {
        clearPreviews(selectedPolygons);

        if (selectedPolygons.isEmpty()) return;

        List<List<LatLng>> polygonPoints = selectedPolygons.stream()
                .map(Polygon::getPoints)
                .collect(Collectors.toList());

        // Create zones from inside out
        List<LatLng> yellowZone = createBufferZone(polygonPoints, Constants.YELLOW_BUFFER_LIMIT);
        List<LatLng> orangeZone = createBufferZone(polygonPoints, Constants.ORANGE_BUFFER_LiMIT);
        List<LatLng> redZone = traceBoundary(selectedPolygons);

        setRedZone(redZone);
        setOrangeZone(orangeZone);
        setYellowZone(yellowZone);

        // Add zones from outside in
        addPreviewPolygon(yellowZone, Colors.YELLOW_ZONE);
        addPreviewPolygon(orangeZone, Colors.ORANGE_ZONE);
        addPreviewPolygon(redZone, Colors.RED_ZONE);

        // Hide grid cells
        selectedPolygons.forEach(polygon -> polygon.setVisible(false));
    }

    public void clearPreviews(List<Polygon> selectedPolygons) {
        selectedPolygons.forEach(polygon -> polygon.setVisible(true));
        previewPolygons.forEach(Polygon::remove);
        previewPolygons.clear();
    }

    private void addPreviewPolygon(List<LatLng> points, int fillColor) {
        Polygon preview = googleMap.addPolygon(new PolygonOptions()
                .addAll(points)
                .strokeWidth(4)
                .strokeColor(Colors.STROKE)
                .fillColor(fillColor));
        previewPolygons.add(preview);
    }

    private List<LatLng> traceBoundary(List<Polygon> selectedPolygons) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry union = createUnionFromPolygons(
                selectedPolygons.stream()
                        .map(Polygon::getPoints)
                        .collect(Collectors.toList()),
                geometryFactory
        );

        return Arrays.stream(union.getBoundary().getCoordinates())
                .map(c -> new LatLng(c.y, c.x))
                .collect(Collectors.toList());
    }

    private List<LatLng> createBufferZone(List<List<LatLng>> polygonPoints, double bufferDistanceMeters) {
        double bufferDegrees = bufferDistanceMeters /
                ((Constants.METERS_PER_LAT + Constants.METERS_PER_LNG) / 2);

        GeometryFactory geometryFactory = new GeometryFactory();
        Geometry union = createUnionFromPolygons(polygonPoints, geometryFactory);

        return createBuffer(union, bufferDegrees);
    }

    private Geometry createUnionFromPolygons(List<List<LatLng>> polygonPoints,
                                             GeometryFactory geometryFactory) {
        List<org.locationtech.jts.geom.Polygon> jtsPolygons = polygonPoints.stream()
                .map(points -> createJTSPolygon(points, geometryFactory))
                .collect(Collectors.toList());

        Geometry union = jtsPolygons.get(0);
        for (int i = 1; i < jtsPolygons.size(); i++) {
            union = union.union(jtsPolygons.get(i));
        }
        return union;
    }

    private org.locationtech.jts.geom.Polygon createJTSPolygon(List<LatLng> points,
                                                               GeometryFactory geometryFactory) {
        Coordinate[] coordinates = points.stream()
                .map(p -> new Coordinate(p.longitude, p.latitude))
                .toArray(Coordinate[]::new);
        coordinates = Arrays.copyOf(coordinates, coordinates.length + 1);
        coordinates[coordinates.length - 1] = coordinates[0];

        LinearRing ring = geometryFactory.createLinearRing(coordinates);
        return geometryFactory.createPolygon(ring);
    }

    private List<LatLng> createBuffer(Geometry geometry, double bufferDegrees) {
        BufferParameters params = new BufferParameters();
        params.setJoinStyle(BufferParameters.JOIN_MITRE);
        params.setQuadrantSegments(8);

        BufferOp bufferOp = new BufferOp(geometry, params);
        Geometry buffer = bufferOp.getResultGeometry(bufferDegrees);

        return Arrays.stream(buffer.getCoordinates())
                .map(c -> new LatLng(c.y, c.x))
                .collect(Collectors.toList());
    }
}

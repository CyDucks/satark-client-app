package org.cyducks.satark.core.grid;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class GridManager {
    private static final double CELL_SIZE = 200.0;
    private static final int GRID_COLOR = Color.argb(30, 33, 150, 243);
    private static final int SELECTED_COLOR = Color.argb(80, 244, 67, 54);

    private static final int BORDER_COLOR = Color.argb(255, 33, 150, 243);
    private static final String TAG = "GridManager";
    private GoogleMap googleMap;
    private final List<Polygon> gridPolygons = new CopyOnWriteArrayList<>();
    private final List<Polygon> selectedPolygons = new CopyOnWriteArrayList<>();
    private LatLngBounds visibleBounds;
    private final GridOverlay gridOverlay;

    public GridManager() {
        gridOverlay = new GridOverlay();
    }

    public void setupListeners() {
        googleMap.setOnCameraIdleListener(() -> {
            visibleBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
            clearGrid();
            drawGrid();
        });
    }

    public void initializeGrid(GoogleMap map) {
        this.googleMap = map;
        setupListeners();
        gridOverlay.initializeSelectionMode(map, gridPolygons);
    }

    public void toggleSelectionMode(boolean enabled) {
        if(!enabled) {
            clearSelection();
        }
    }

    private void clearGrid() {
        for(Polygon polygon : gridPolygons) {
            polygon.remove();
        }
        gridPolygons.clear();
    }

    public void clearSelection() {
        for(Polygon polygon : selectedPolygons) {
            deselectPolygon(polygon);
        }
        selectedPolygons.clear();
    }
    public List<Polygon> getGridCells() {
        return this.gridPolygons;
    }

    private void drawGrid() {
        if(visibleBounds == null) return;

        LatLng sw = visibleBounds.southwest;
        LatLng ne = visibleBounds.northeast;

        double latStep = calculateLatStep();
        double lngStep = calculateLngStep(sw.latitude);

        for(double lat = sw.latitude; lat < ne.latitude; lat+=latStep) {
            for(double lng = sw.longitude; lng < ne.longitude; lng+=lngStep) {
                drawCellPolygon(new LatLng(lat, lng));
            }
        }
    }

    private void drawCellPolygon(LatLng latLng) {
        PolygonOptions options = new PolygonOptions()
                .add(calculateCellCorners(latLng))
                .strokeWidth(2)
                .clickable(true)
                .strokeColor(BORDER_COLOR)
                .fillColor(GRID_COLOR);

        Polygon polygon = googleMap.addPolygon(options);
        polygon.setTag(createCellMetaData(latLng));
        gridPolygons.add(polygon);
    }

    public void handleCellSelection(Polygon polygon) {
        if(selectedPolygons.contains(polygon)) {
            // deselectPolygon(polygon);
        } else {
            selectPolygon(polygon);
        }
    }

    private void selectPolygon(Polygon polygon) {
        polygon.setFillColor(SELECTED_COLOR);
        selectedPolygons.add(polygon);
    }

    private void deselectPolygon(Polygon polygon) {
        polygon.setFillColor(GRID_COLOR);
        selectedPolygons.remove(polygon);
    }

    public void logSelectedRegion() {
        if (selectedPolygons.isEmpty()) return;

        List<CellMetaData> selectedCells = selectedPolygons.stream()
                .map(polygon -> (CellMetaData) polygon.getTag())
                .collect(Collectors.toList());

        Log.d("GridManager", "Selected Cells: " + selectedCells.size());
        for (CellMetaData cell : selectedCells) {
            Log.d("GridManager", cell.toString());
        }
    }

    private CellMetaData createCellMetaData(LatLng latLng) {
        return new CellMetaData(
                UUID.randomUUID().toString(),
                latLng,
                CELL_SIZE
        );
    }

    private LatLng[] calculateCellCorners(LatLng center) {
        double halfWidth = calculateLngStep(visibleBounds.southwest.latitude) / 2;
        double halfHeight = calculateLatStep() / 2;

        return new LatLng[] {
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth)
        };
    }



    private double calculateLatStep() {
        return CELL_SIZE / 111000.0;
    }

    private double calculateLngStep(double latitude) {
        return CELL_SIZE / (111000.0 * Math.cos(Math.toRadians(latitude)));
    }

    public List<Polygon> getSelectedPolygons() {
        return selectedPolygons;
    }


    public static class CellMetaData {
        String id;
        LatLng center;
        double size;

        public CellMetaData(String id, LatLng center, double size) {
            this.id = id;
            this.center = center;
            this.size = size;

        }

        @NonNull
        @Override
        public String toString() {
            return "CellMetaData{" +
                    "id='" + id + '\'' +
                    ", center=" + center +
                    ", size=" + size +
                    '}';
        }

    }

}

package org.cyducks.satark.core.grid;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class GridOverlay {
    private GoogleMap googleMap;
    private List<Polygon> gridCells;
    private List<Polygon> selectedCells;

    private boolean isSelecting = false;
    private LatLng startPoint;
    private Polygon selectionRectangle;
    private Projection projection;

    public GridOverlay() {
        selectedCells = new ArrayList<>();
    }

    public void initializeSelectionMode(GoogleMap googleMap, List<Polygon> gridCells) {
        this.googleMap = googleMap;
        this.gridCells = gridCells;
        this.projection = googleMap.getProjection();

        googleMap.setOnMapLongClickListener(latLng -> {
            startPoint = latLng;
            isSelecting = true;
        });

        googleMap.setOnMapClickListener(endPoint -> {
            if(isSelecting) {
                completeRectangleSelection(endPoint);
                isSelecting = false;
            }
        });
    }

    private void completeRectangleSelection(LatLng endpoint) {
        if(selectionRectangle != null) {
            selectionRectangle.remove();
        }

        LatLngBounds selectionBounds = LatLngBounds.builder()
                .include(startPoint)
                .include(endpoint)
                .build();

        PolygonOptions rectangleOptions = new PolygonOptions()
                .add(startPoint)
                .add(new LatLng(startPoint.latitude, endpoint.longitude))
                .add(endpoint)
                .add(new LatLng(endpoint.latitude, startPoint.longitude))
                .fillColor(Color.argb(50 , 33, 150, 243))
                .strokeColor(Color.BLUE)
                .strokeWidth(5);

        selectionRectangle = googleMap.addPolygon(rectangleOptions);

        selectCellsInBounds(selectionBounds);

    }

    private void selectCellsInBounds(LatLngBounds bounds) {
        deselectAll();

        for(Polygon cell : gridCells) {
            LatLng cellCenter = calculatePolygonCenter(cell);

            if(bounds.contains(cellCenter)) {
                selectCell(cell);
            }
        }
    }

    private void deselectAll() {
    }

    private LatLng calculatePolygonCenter(Polygon cell) {
        List<LatLng> points = cell.getPoints();

        double centerLat = points.stream()
                .mapToDouble(latLng -> latLng.latitude)
                .average()
                .orElse(0);

        double centerLng = points.stream()
                .mapToDouble(latLng -> latLng.longitude)
                .average()
                .orElse(0);

        return new LatLng(centerLat, centerLng);
    }

    private void selectCell(Polygon cell) {
        cell.setFillColor(Color.argb(100, 244, 67, 54));
        selectedCells.add(cell);
    }
}

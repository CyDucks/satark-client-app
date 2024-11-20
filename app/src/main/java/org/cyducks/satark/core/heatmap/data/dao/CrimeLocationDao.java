package org.cyducks.satark.core.heatmap.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.cyducks.satark.core.heatmap.data.entity.CrimeLocation;

import java.util.List;

@Dao
public interface CrimeLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CrimeLocation> locationList);

    @Query("SELECT * FROM crime_location")
    List<CrimeLocation> getAllLocations();

    @Query("DELETE FROM crime_location")
    void deleteAll();

    @Query("SELECT * FROM crime_location WHERE" +
            " latitude BETWEEN :minLat AND :maxLat AND" +
            " longitude BETWEEN :minLng AND :maxLng " +
            "LIMIT 1000")
    List<CrimeLocation> getLocationsInBounds(double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT * FROM crime_location WHERE " +
            "latitude BETWEEN :minLat AND :maxLat AND " +
            "longitude BETWEEN :minLng AND :maxLng AND " +
            "crimeDescription IN (:crimeTypes) " +
            "LIMIT 1000")
    List<CrimeLocation> getFilteredLocations(double minLat, double maxLat, double minLng, double maxLng, List<String> crimeTypes);
}

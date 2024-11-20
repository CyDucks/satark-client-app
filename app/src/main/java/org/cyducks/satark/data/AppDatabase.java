package org.cyducks.satark.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import org.cyducks.satark.core.heatmap.data.dao.CrimeLocationDao;
import org.cyducks.satark.core.heatmap.data.dao.FileMetaDataDao;
import org.cyducks.satark.core.heatmap.data.entity.CrimeLocation;
import org.cyducks.satark.core.heatmap.data.entity.FIleMetadata;

@Database(entities = {CrimeLocation.class, FIleMetadata.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CrimeLocationDao crimeLocationDao();
    public abstract FileMetaDataDao fileMetaDataDao();
}
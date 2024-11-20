package org.cyducks.satark.core.heatmap.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.cyducks.satark.core.heatmap.data.entity.FIleMetadata;

@Dao
public interface FileMetaDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FIleMetadata metadata);

    @Query("SELECT * FROM file_metadata WHERE fileName = :fileName")
    FIleMetadata getMetadata(String fileName);

}

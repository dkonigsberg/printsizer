package org.logicprobe.printsizer.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.List;

@Dao
public interface EnlargerProfileDao {
    @Query("SELECT * FROM enlarger_profiles ORDER BY name, lens_focal_length, description, id")
    LiveData<List<EnlargerProfileEntity>> loadAllEnlargerProfiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EnlargerProfileEntity enlargerProfile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EnlargerProfileEntity> enlargerProfiles);

    @Query("DELETE FROM " + EnlargerProfileEntity.TABLE_NAME + " WHERE id = :enlargerProfileId")
    int deleteById(int enlargerProfileId);

    @Query("DELETE FROM " + EnlargerProfileEntity.TABLE_NAME + " WHERE id IN(:enlargerProfileIds)")
    int deleteByIds(int[] enlargerProfileIds);

    @Query("SELECT * FROM " + EnlargerProfileEntity.TABLE_NAME + " WHERE id = :enlargerProfileId")
    LiveData<EnlargerProfileEntity> loadEnlargerProfile(int enlargerProfileId);

    @Query("SELECT * FROM " + EnlargerProfileEntity.TABLE_NAME + " WHERE id = :enlargerProfileId")
    EnlargerProfileEntity loadEnlargerProfileSync(int enlargerProfileId);
}

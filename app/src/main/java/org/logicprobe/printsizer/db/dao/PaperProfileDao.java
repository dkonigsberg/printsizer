package org.logicprobe.printsizer.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.List;

@Dao
public interface PaperProfileDao {
    @Query("SELECT COUNT(*) FROM "+ PaperProfileEntity.TABLE_NAME)
    LiveData<Integer> numPaperProfiles();

    @Query("SELECT * FROM " + PaperProfileEntity.TABLE_NAME + " ORDER BY name, description, id")
    LiveData<List<PaperProfileEntity>> loadAllPaperProfiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PaperProfileEntity paperProfile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<PaperProfileEntity> paperProfiles);

    @Query("DELETE FROM " + PaperProfileEntity.TABLE_NAME + " WHERE id = :paperProfileId")
    int deleteById(int paperProfileId);

    @Query("DELETE FROM " + PaperProfileEntity.TABLE_NAME + " WHERE id IN(:paperProfileIds)")
    int deleteByIds(int[] paperProfileIds);

    @Query("SELECT * FROM " + PaperProfileEntity.TABLE_NAME + " WHERE id = :paperProfileId")
    LiveData<PaperProfileEntity> loadPaperProfile(int paperProfileId);

    @Query("SELECT * FROM " + PaperProfileEntity.TABLE_NAME + " WHERE id = :paperProfileId")
    PaperProfileEntity loadPaperProfileSync(int paperProfileId);
}

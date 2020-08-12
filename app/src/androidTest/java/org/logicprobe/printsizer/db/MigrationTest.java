package org.logicprobe.printsizer.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicprobe.printsizer.LiveDataTestUtil;
import org.logicprobe.printsizer.db.dao.EnlargerProfileDao;
import org.logicprobe.printsizer.db.dao.PaperProfileDao;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.EnlargerProfile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.logicprobe.printsizer.db.AppDatabase.MIGRATION_1_2;
import static org.logicprobe.printsizer.db.AppDatabase.MIGRATION_2_3;

@RunWith(AndroidJUnit4.class)
public class MigrationTest {
    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    public MigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                Objects.requireNonNull(AppDatabase.class.getCanonicalName()),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        ContentValues values = new ContentValues();
        values.put("name", "Name");
        values.put("description", "Description");
        values.put("height_measurement_offset", 0.0d);
        values.put("lens_focal_length", 50.0d);
        values.put("smaller_test_distance", 300.0d);
        values.put("smaller_test_time", 10.0d);
        values.put("larger_test_distance", 600.0d);
        values.put("larger_test_time", 20.0d);
        db.insert("enlarger_profiles", SQLiteDatabase.CONFLICT_REPLACE, values);

        db.close();

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2);

        EnlargerProfileDao enlargerDao = getMigratedRoomDatabase().enlargerProfileDao();
        EnlargerProfile profile = enlargerDao.loadEnlargerProfileSync(1);
        assertEquals(profile.getName(), "Name");
        assertEquals(profile.getDescription(), "Description");
        assertEquals(profile.getHeightMeasurementOffset(), 0.0d, 0.001d);
        assertTrue(profile.hasTestExposures());
        assertEquals(profile.getSmallerTestDistance(), 300.0d, 0.001d);
        assertEquals(profile.getSmallerTestTime(), 10.0d, 0.001d);
        assertEquals(profile.getLargerTestDistance(), 600.0d, 0.001d);
        assertEquals(profile.getLargerTestTime(), 20.0d, 0.001d);
    }

    @Test
    public void migrate2To3() throws IOException, InterruptedException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);
        db.close();
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_2_3);

        PaperProfileDao paperDao = getMigratedRoomDatabase().paperProfileDao();
        List<PaperProfileEntity> profiles = LiveDataTestUtil.getValue(paperDao.loadAllPaperProfiles());
        assertTrue(profiles.isEmpty());
    }

    @Test
    public void migrateAll() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);
        db.close();

        AppDatabase database = Room.databaseBuilder(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                AppDatabase.class,
                TEST_DB)
                .addMigrations(ALL_MIGRATIONS).build();
        database.getOpenHelper().getWritableDatabase();
        database.close();
    }

    private AppDatabase getMigratedRoomDatabase() {
        AppDatabase database = Room.databaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class, TEST_DB)
                .addMigrations(ALL_MIGRATIONS)
                .build();
        helper.closeWhenFinished(database);
        return database;
    }

    private static final Migration[] ALL_MIGRATIONS = new Migration[]{
            MIGRATION_1_2, MIGRATION_2_3};
}

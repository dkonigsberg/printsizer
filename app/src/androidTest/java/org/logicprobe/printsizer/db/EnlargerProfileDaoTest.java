package org.logicprobe.printsizer.db;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicprobe.printsizer.LiveDataTestUtil;
import org.logicprobe.printsizer.db.dao.EnlargerProfileDao;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EnlargerProfileDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase database;
    private EnlargerProfileDao enlargerDao;

    @Before
    public void initDb() throws Exception {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        enlargerDao = database.enlargerProfileDao();
    }

    @After
    public void closeDb() throws Exception {
        database.close();
    }

    @Test
    public void getProfilesWhenEmpty() throws InterruptedException {
        List<EnlargerProfileEntity> profiles = LiveDataTestUtil.getValue(enlargerDao.loadAllEnlargerProfiles());
        assertTrue(profiles.isEmpty());
    }

    @Test
    public void getEnlargersAfterInserted() throws InterruptedException {
        enlargerDao.insertAll(TestData.ENLARGERS);

        List<EnlargerProfileEntity> profiles = LiveDataTestUtil.getValue(enlargerDao.loadAllEnlargerProfiles());
        assertEquals(profiles.size(), TestData.ENLARGERS.size());
    }

    @Test
    public void getEnlargerById() throws InterruptedException {
        enlargerDao.insertAll(TestData.ENLARGERS);

        EnlargerProfileEntity profile = LiveDataTestUtil.getValue(enlargerDao.loadEnlargerProfile(2));
        assertEquals(profile.getName(), TestData.ENLARGER_ENTITY2.getName());
        assertEquals(profile.getDescription(), TestData.ENLARGER_ENTITY2.getDescription());
        assertEquals(profile.getHeightMeasurementOffset(), TestData.ENLARGER_ENTITY2.getHeightMeasurementOffset(), 0.001d);
        assertEquals(profile.getLensFocalLength(), TestData.ENLARGER_ENTITY2.getLensFocalLength(), 0.001d);
        assertEquals(profile.hasTestExposures(), TestData.ENLARGER_ENTITY2.hasTestExposures());
    }
}

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
import org.logicprobe.printsizer.db.dao.PaperProfileDao;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PaperProfileDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase database;
    private PaperProfileDao paperDao;

    @Before
    public void initDb() throws Exception {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        paperDao = database.paperProfileDao();
    }

    @After
    public void closeDb() throws Exception {
        database.close();
    }

    @Test
    public void getProfilesWhenEmpty() throws InterruptedException {
        List<PaperProfileEntity> profiles = LiveDataTestUtil.getValue(paperDao.loadAllPaperProfiles());
        assertTrue(profiles.isEmpty());
    }

    @Test
    public void getEnlargersAfterInserted() throws InterruptedException {
        paperDao.insertAll(TestData.PAPERS);

        List<PaperProfileEntity> profiles = LiveDataTestUtil.getValue(paperDao.loadAllPaperProfiles());
        assertEquals(profiles.size(), TestData.PAPERS.size());
    }

    @Test
    public void getEnlargerById() throws InterruptedException {
        paperDao.insertAll(TestData.PAPERS);

        PaperProfileEntity profile = LiveDataTestUtil.getValue(paperDao.loadPaperProfile(2));
        assertEquals(profile.getName(), TestData.PAPER_ENTITY2.getName());
        assertEquals(profile.getDescription(), TestData.PAPER_ENTITY2.getDescription());
        assertEquals(profile.getGrade00(), TestData.PAPER_ENTITY2.getGrade00());
        assertEquals(profile.getGrade0(), TestData.PAPER_ENTITY2.getGrade0());
        assertEquals(profile.getGrade1(), TestData.PAPER_ENTITY2.getGrade1());
        assertEquals(profile.getGrade2(), TestData.PAPER_ENTITY2.getGrade2());
        assertEquals(profile.getGrade3(), TestData.PAPER_ENTITY2.getGrade3());
        assertEquals(profile.getGrade4(), TestData.PAPER_ENTITY2.getGrade4());
        assertEquals(profile.getGrade5(), TestData.PAPER_ENTITY2.getGrade5());
        assertEquals(profile.getGradeNone(), TestData.PAPER_ENTITY2.getGradeNone());
    }
}

package org.logicprobe.printsizer.db;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicprobe.printsizer.db.dao.StockPaperProfileDao;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StockPaperProfileDaoTest {

    @Test
    public void loadAllStockPaperProfiles() {
        StockPaperProfileDao paperDao = new StockPaperProfileDao(ApplicationProvider.getApplicationContext());
        List<PaperProfileEntity> profiles = paperDao.loadAllPaperProfiles();

        assertNotNull(profiles);
        assertTrue(profiles.size() > 0);
        for (PaperProfileEntity profileEntity : profiles) {
            assertNotNull(profileEntity);
            assertNotNull(profileEntity.getName());
        }
    }
}

package org.logicprobe.printsizer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import org.logicprobe.printsizer.db.AppDatabase;
import org.logicprobe.printsizer.db.dao.EnlargerProfileDao;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.List;

public class DataRepository {
    private static DataRepository instance;

    private final AppDatabase database;
    private final AppExecutors appExecutors;

    private EnlargerProfileDao enlargerProfileDao;

    private MediatorLiveData<List<EnlargerProfileEntity>> observableEnlargers;

    private DataRepository(final AppDatabase database, final AppExecutors appExecutors) {
        this.database = database;
        this.appExecutors = appExecutors;
        this.enlargerProfileDao = database.enlargerProfileDao();

        observableEnlargers = new MediatorLiveData<>();
        observableEnlargers.addSource(this.database.enlargerProfileDao().loadAllEnlargerProfiles(),
                new Observer<List<EnlargerProfileEntity>>() {
                    @Override
                    public void onChanged(List<EnlargerProfileEntity> enlargerProfileEntities) {
                        if (DataRepository.this.database.getDatabaseCreated().getValue() != null) {
                            observableEnlargers.postValue(enlargerProfileEntities);
                        }
                    }
                });
    }

    public static DataRepository getInstance(final AppDatabase database, final AppExecutors appExecutors) {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(database, appExecutors);
                }
            }
        }
        return instance;
    }

    public LiveData<List<EnlargerProfileEntity>> getEnlargerProfiles() {
        return observableEnlargers;
    }

    public LiveData<EnlargerProfileEntity> loadEnlargerProfile(final int enlargerProfileId) {
        return database.enlargerProfileDao().loadEnlargerProfile(enlargerProfileId);
    }

    public void insert(final EnlargerProfileEntity enlargerProfile) {
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                enlargerProfileDao.insert(enlargerProfile);
            }
        });
    }

    public void deleteEnlargerProfileById(final int enlargerProfileId) {
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                enlargerProfileDao.deleteById(enlargerProfileId);
            }
        });
    }

    public void deleteEnlargerProfilesById(final int[] enlargerProfileIds) {
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                enlargerProfileDao.deleteByIds(enlargerProfileIds);
            }
        });
    }
}

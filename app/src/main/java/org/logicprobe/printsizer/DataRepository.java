package org.logicprobe.printsizer;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.logicprobe.printsizer.db.AppDatabase;
import org.logicprobe.printsizer.db.dao.EnlargerProfileDao;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.List;

public class DataRepository {
    private static final String TAG = DataRepository.class.getSimpleName();
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

    public LiveData<Integer> insert(final EnlargerProfileEntity enlargerProfile) {
        final MutableLiveData<Integer> liveId = new MutableLiveData<>();
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                long profileId = enlargerProfileDao.insert(enlargerProfile);
                Log.d(TAG, "Inserted profile: " + enlargerProfile.getName() + " -> " + profileId);
                liveId.postValue((int)profileId);
            }
        });
        return liveId;
    }

    public void deleteEnlargerProfileById(final int enlargerProfileId) {
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                enlargerProfileDao.deleteById(enlargerProfileId);
                Log.d(TAG, "Deleted profile: " + enlargerProfileId);
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

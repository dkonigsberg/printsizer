package org.logicprobe.printsizer;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.logicprobe.printsizer.db.AppDatabase;
import org.logicprobe.printsizer.db.dao.EnlargerProfileDao;
import org.logicprobe.printsizer.db.dao.PaperProfileDao;
import org.logicprobe.printsizer.db.dao.StockPaperProfileDao;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.ArrayList;
import java.util.List;

public class DataRepository {
    private static final String TAG = DataRepository.class.getSimpleName();
    private static DataRepository instance;

    private final AppDatabase database;
    private final AppExecutors appExecutors;

    private EnlargerProfileDao enlargerProfileDao;
    private PaperProfileDao paperProfileDao;
    private StockPaperProfileDao stockPaperProfileDao;

    private MediatorLiveData<List<EnlargerProfileEntity>> observableEnlargers;
    private MediatorLiveData<List<PaperProfileEntity>> observablePapers;
    private MediatorLiveData<Integer> observableNumPapers;

    private DataRepository(final Context context, final AppDatabase database, final AppExecutors appExecutors) {
        this.database = database;
        this.appExecutors = appExecutors;
        this.enlargerProfileDao = database.enlargerProfileDao();
        this.paperProfileDao = database.paperProfileDao();
        this.stockPaperProfileDao = new StockPaperProfileDao(context);

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

        observablePapers = new MediatorLiveData<>();
        observablePapers.addSource(this.database.paperProfileDao().loadAllPaperProfiles(),
                new Observer<List<PaperProfileEntity>>() {
                    @Override
                    public void onChanged(List<PaperProfileEntity> paperProfileEntities) {
                        if (DataRepository.this.database.getDatabaseCreated().getValue() != null) {
                            observablePapers.postValue(paperProfileEntities);
                        }
                    }
                });

        observableNumPapers = new MediatorLiveData<>();
        observableNumPapers.addSource(this.database.paperProfileDao().numPaperProfiles(),
                new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer numPapers) {
                        if (DataRepository.this.database.getDatabaseCreated().getValue() != null) {
                            if (numPapers == null) {
                                numPapers = Integer.valueOf(0);
                            }
                            observableNumPapers.postValue(numPapers);
                        }
                    }
                });
    }

    public static DataRepository getInstance(final Context context, final AppDatabase database, final AppExecutors appExecutors) {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(context, database, appExecutors);
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
    
    public LiveData<List<PaperProfileEntity>> getPaperProfiles() {
        return observablePapers;
    }

    public LiveData<Integer> numPaperProfiles() {
        return observableNumPapers;
    }

    public LiveData<List<PaperProfileEntity>> getStockPaperProfiles() {
        final MutableLiveData<List<PaperProfileEntity>> stockProfiles = new MutableLiveData<>();
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                List<PaperProfileEntity> paperProfiles = stockPaperProfileDao.loadAllPaperProfiles();
                Log.d(TAG, "Loaded " + paperProfiles + " stock paper profiles");
                stockProfiles.postValue(paperProfiles);
            }
        });
        return stockProfiles;
    }

    public LiveData<PaperProfileEntity> loadPaperProfile(final int paperProfileId) {
        return database.paperProfileDao().loadPaperProfile(paperProfileId);
    }

    public LiveData<Integer> insert(final PaperProfileEntity paperProfile) {
        final MutableLiveData<Integer> liveId = new MutableLiveData<>();
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                long profileId = paperProfileDao.insert(paperProfile);
                Log.d(TAG, "Inserted profile: " + paperProfile.getName() + " -> " + profileId);
                liveId.postValue((int)profileId);
            }
        });
        return liveId;
    }

    public LiveData<List<Integer>> insertAll(final List<PaperProfileEntity> paperProfiles) {
        final MutableLiveData<List<Integer>> liveIds = new MutableLiveData<>();
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                long[] profileIds = paperProfileDao.insertAll(paperProfiles);
                Log.d(TAG, "Inserted " + profileIds.length + " profiles");
                List<Integer> resultList = new ArrayList<>(profileIds.length);
                for (long id : profileIds) {
                    resultList.add((int)id);
                }
                liveIds.postValue(resultList);
            }
        });
        return liveIds;
    }

    public void deletePaperProfileById(final int paperProfileId) {
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                paperProfileDao.deleteById(paperProfileId);
                Log.d(TAG, "Deleted profile: " + paperProfileId);
            }
        });
    }

    public void deletePaperProfilesById(final int[] paperProfileIds) {
        appExecutors.database().execute(new Runnable() {
            @Override
            public void run() {
                paperProfileDao.deleteByIds(paperProfileIds);
            }
        });
    }
}

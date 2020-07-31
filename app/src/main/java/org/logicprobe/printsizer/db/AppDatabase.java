package org.logicprobe.printsizer.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.logicprobe.printsizer.AppExecutors;
import org.logicprobe.printsizer.db.dao.EnlargerProfileDao;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

@Database(entities = {EnlargerProfileEntity.class}, version = 2)
public abstract class AppDatabase  extends RoomDatabase {
    private static AppDatabase instance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "printsizer-db";

    public abstract EnlargerProfileDao enlargerProfileDao();

    private final MutableLiveData<Boolean> isDatabaseCreated = new MutableLiveData<>();

    public static AppDatabase getInstance(final Context context, final AppExecutors executors) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = buildDatabase(context.getApplicationContext(), executors);
                    instance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private static AppDatabase buildDatabase(final Context appContext,
                                             final AppExecutors executors) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.database().execute(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase database = AppDatabase.getInstance(appContext, executors);
                                database.setDatabaseCreated();
                            }
                        });
                    }
                })
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        isDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return isDatabaseCreated;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE enlarger_profiles ADD COLUMN has_test_exposures INTEGER NOT NULL DEFAULT 1");
        }
    };
}

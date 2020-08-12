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
import org.logicprobe.printsizer.db.dao.PaperProfileDao;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

@Database(entities = {
        EnlargerProfileEntity.class, PaperProfileEntity.class},
        version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "printsizer-db";

    public abstract EnlargerProfileDao enlargerProfileDao();
    public abstract PaperProfileDao paperProfileDao();

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
                .addMigrations(MIGRATION_2_3)
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

    @VisibleForTesting
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE enlarger_profiles ADD COLUMN has_test_exposures INTEGER NOT NULL DEFAULT 1");
        }
    };

    @VisibleForTesting
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS paper_profiles (" +
                    "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "'name' TEXT, 'description' TEXT, " +
                    "'00_iso_p' INTEGER, '00_iso_r' INTEGER, '0_iso_p' INTEGER, '0_iso_r' INTEGER, " +
                    "'1_iso_p' INTEGER, '1_iso_r' INTEGER, '2_iso_p' INTEGER, '2_iso_r' INTEGER, " +
                    "'3_iso_p' INTEGER, '3_iso_r' INTEGER, '4_iso_p' INTEGER, '4_iso_r' INTEGER, " +
                    "'5_iso_p' INTEGER, '5_iso_r' INTEGER, " +
                    "'none_iso_p' INTEGER, 'none_iso_r' INTEGER)");
        }
    };
}

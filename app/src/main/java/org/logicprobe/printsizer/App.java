package org.logicprobe.printsizer;

import android.app.Application;

import org.logicprobe.printsizer.db.AppDatabase;

public class App extends Application {
    private AppExecutors appExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        appExecutors = new AppExecutors();
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, appExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase(), appExecutors);
    }
}

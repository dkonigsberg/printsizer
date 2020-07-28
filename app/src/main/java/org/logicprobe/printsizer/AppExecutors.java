package org.logicprobe.printsizer;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private final Executor executorMainThread;
    private final Executor executorDatabase;

    private AppExecutors(Executor mainThread, Executor database) {
        this.executorMainThread = mainThread;
        this.executorDatabase = database;
    }

    public AppExecutors() {
        this(new MainThreadExecutor(), Executors.newSingleThreadExecutor());
    }

    public Executor database() {
        return executorDatabase;
    }

    public Executor mainThread() {
        return executorMainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}

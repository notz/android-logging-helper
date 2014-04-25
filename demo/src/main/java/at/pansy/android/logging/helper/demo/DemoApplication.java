package at.pansy.android.logging.helper.demo;

import android.app.Application;

import java.util.logging.Level;

import at.pansy.android.logging.helper.LoggingHelper;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LoggingHelper.configure(this, "logging-demo", true, Level.FINE);
        LoggingHelper.setupUncaughtExceptionHandler();
    }

    @Override
    public void onTerminate() {
        LoggingHelper.flush();
        super.onTerminate();
    }
}

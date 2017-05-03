Android logging helper
======================

Helper functions to make java logging funny on Android

Supports <b>logcat</b>, file logging with <b>memory buffer</b>, <b>sharing</b> zipped log file and install uncaught exception handler.

The lib uses native java logging and is <b>very small</b>.

Usage
-----

Add the content provider to your application manifest

<pre>
&lt;provider
	android:name="at.pansy.android.logging.helper.LogFileProvider"
	android:authorities="${applicationId}.logfileprovider"
	android:enabled="true"
	android:exported="false"
	android:grantUriPermissions="true" /&gt;
</pre>

Initialize logging in your application class

<pre>
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
</pre>

In your application use java logging

<pre>
logger.config("This is a debug log entry");
logger.info("This is a info log entry");
logger.warning("This is a warning");
logger.severe("This is a error");

try {
    throw new RuntimeException("This is a runtime exception");
} catch (Exception e) {
    logger.log(Level.SEVERE, e.getMessage(), e);
}

logger.log(Level.WARNING, "this is a formatted {0}", new Object[] { "message"});
</pre>

It's easy to share your log

<pre>
LoggingHelper.shareLog(MainActivity.this, "help@yourcompany.com", "This is a logging demo");
</pre>




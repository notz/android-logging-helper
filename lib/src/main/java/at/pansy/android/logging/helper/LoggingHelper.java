package at.pansy.android.logging.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import java.io.FileNotFoundException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class LoggingHelper {

    private static Logger logger = Logger.getLogger(LoggingHelper.class.getName());

    public static void configure(Context context, String tag, boolean logcatEnabled, Level level) {
        configure(new AndroidHandler(context, tag, logcatEnabled, level));
    }

    public static void configure(Context context, String tag, boolean logcatEnabled, Level level, int maxFileSize, int memorySize, Level pushLevel) {
        configure(new AndroidHandler(context, tag, logcatEnabled, level, maxFileSize, memorySize, pushLevel));
    }

    public static void configure(AndroidHandler androidHandler) {
        Logger rootLogger = Logger.getLogger("");

        for (Handler handler : rootLogger.getHandlers()) {
            handler.flush();
            handler.close();
            rootLogger.removeHandler(handler);
        }

        rootLogger.setLevel(androidHandler.getLevel());
        rootLogger.addHandler(androidHandler);
    }

    public static void flush() {
        AndroidHandler androidHandler = getAndroidHandler();
        if (androidHandler != null) {
            androidHandler.flush();
        }
    }

    public static void setupUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()));
    }

    public static void shareLog(final Context context) {
        shareLog(context, null, null, null);
    }

    public static void shareLog(final Context context, final String email, final String subject) {
        shareLog(context, email, subject, null);
    }

    public static void shareLog(final Context context, final String email, final String subject, final String body) {
        AndroidHandler androidHandler = getAndroidHandler();
        if (androidHandler != null) {
            androidHandler.flush();

            doShareLog(context, androidHandler.getTag(), email, subject, body);
        }
    }

    public static ParcelFileDescriptor getLogFileDescriptor(final Context context) throws FileNotFoundException {
        AndroidHandler androidHandler = getAndroidHandler();
        if (androidHandler == null) {
            throw new IllegalStateException();
        }

        return context.getContentResolver().openFileDescriptor(LogFileProvider.createFileUri(context, androidHandler.getTag()), "r");
    }

    private static void doShareLog(final Context context, final String tag, final String email, final String subject, final String body) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/x-gzip");
        if (email != null) {
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
        }
        if (subject != null) {
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        }
        if (body != null) {
            // According to http://developer.android.com/reference/android/content/Intent.html#ACTION_SEND a send-intent should
            // either have EXTRA_TEXT or EXTRA_STREAM set, both setting both seems to be respected by most receivers (e.g. GMail)
            intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        }
        intent.putExtra(android.content.Intent.EXTRA_STREAM, LogFileProvider.createFileUri(context, tag));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // TODO add callback
        }
    }

    private static AndroidHandler getAndroidHandler() {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof AndroidHandler) {
                return (AndroidHandler) handler;
            }
        }
        return null;
    }
}

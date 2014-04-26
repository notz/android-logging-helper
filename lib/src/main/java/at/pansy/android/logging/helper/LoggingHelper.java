package at.pansy.android.logging.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

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
        shareLog(context, null, null);
    }

    public static void shareLog(final Context context, final String email, final String subject) {

        AndroidHandler androidHandler = getAndroidHandler();
        if (androidHandler != null) {
            androidHandler.flush();

            shareLog(context, androidHandler.getTag(), email, subject);
        }
    }

    private static void shareLog(final Context context, final String tag, final String email, final String subject) {

        new AsyncTask<Void, Void, Uri>() {
            @Override
            protected Uri doInBackground(Void... params) {
                BufferedReader reader;
                BufferedWriter writer;

                try {
                    File cacheDir = context.getCacheDir();
                    if (cacheDir != null) {

                        File zipFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + tag + ".log.gz");
                        writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(zipFile))));

                        ArrayList<File> logFiles = new ArrayList<>();
                        for (int i = 1; i >= 0; i--) {
                            File logFile = new File(cacheDir.getAbsolutePath() + File.separator + tag + "." + i + ".log");
                            if (logFile.exists()) {

                                reader = new BufferedReader(new FileReader(logFile));

                                String line;
                                while ((line = reader.readLine()) != null) {
                                    writer.write(line);
                                    writer.newLine();
                                }

                                reader.close();
                            }
                        }

                        writer.close();

                        return Uri.fromFile(zipFile);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Uri result) {
                if (result != null) {
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("application/x-gzip");
                    if (email != null) {
                        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                    }
                    if (subject != null) {
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                    }
                    intent.putExtra(android.content.Intent.EXTRA_STREAM, result);

                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // TODO add callback
                    }
                }
            }
        }.execute();
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

package at.pansy.android.logging.helper;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

public class AndroidHandler extends Handler {

    private static final int MAX_FILE_SIZE = 128 * 1024;
    private static final int MEMORY_SIZE = 1000;

    private MemoryHandler memoryHandler;
    private FileHandler fileHandler;

    private String tag;
    private boolean logcatEnabled;
    private String packageName;

    public AndroidHandler(Context context, String tag, boolean logcatEnabled, Level level) {
        this(context, tag, logcatEnabled, level, MAX_FILE_SIZE, MEMORY_SIZE, Level.WARNING);
    }

    public AndroidHandler(Context context, String tag, boolean logcatEnabled, Level level, int maxFileSize, int memorySize, Level pushLevel) {
        this.tag = tag;
        this.logcatEnabled = logcatEnabled;
        this.packageName = context.getPackageName();

        setLevel(level);

        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null) {
                String fileName = cacheDir.getAbsolutePath() + File.separator + tag + ".%g.log";
                fileHandler = new FileHandler(fileName, maxFileSize, 2, true);
                fileHandler.setLevel(level);

                if (pushLevel.intValue() > level.intValue() && memorySize > 1) {
                    memoryHandler = new MemoryHandler(fileHandler, memorySize, pushLevel);
                }
            }
        } catch (IOException e) {
            Log.e(tag, "Error creating file handler.", e);
        }

        AndroidFormatter androidFormatter = new AndroidFormatter();
        setFormatter(androidFormatter);
    }

    public String getTag() {
        return tag;
    }

    @Override
    public void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }

    @Override
    public void flush() {

        if (memoryHandler != null) {
            memoryHandler.push();
        }

        if (fileHandler != null) {
            fileHandler.flush();
        }
    }

    @Override
    public void setFormatter(Formatter newFormatter) {
        super.setFormatter(newFormatter);

        if (fileHandler != null) {
            fileHandler.setFormatter(newFormatter);
        }
    }

    @Override
    public void publish(LogRecord logRecord) {

        String message = getFormatter().formatMessage(logRecord);
        logRecord.setMessage(message);

        if (memoryHandler != null) {
            memoryHandler.publish(logRecord);
        } else if (fileHandler != null) {
            fileHandler.publish(logRecord);
        }

        if (logcatEnabled) {

            try {
                Log.println(getLogcatLevel(logRecord.getLevel()), tag, message);
            } catch (RuntimeException e) {
                Log.e(tag, "Error logging message.", e);
            }
        }
    }

    private int getLogcatLevel(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return Log.ERROR;
        } else if (value >= Level.WARNING.intValue()) {
            return Log.WARN;
        } else if (value >= Level.INFO.intValue()) {
            return Log.INFO;
        } else if (value >= Level.FINE.intValue()) {
            return Log.DEBUG;
        } else {
            return Log.VERBOSE;
        }
    }

    private class AndroidFormatter extends Formatter {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

        @Override
        public String format(LogRecord logRecord) {
            return dateFormat.format(new Date(logRecord.getMillis()))
                   + " - "
                   + "[" + logRecord.getLevel().getName() + "]"
                   + " " + logRecord.getMessage();
        }

        @Override
        public String formatMessage(LogRecord logRecord) {

            String threadName = Thread.currentThread().getName();
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

            int lineNumber = 0;
            for (int i = 2; i < stackTraceElements.length; i++) {
                StackTraceElement element = stackTraceElements[i];
                if (element.getClassName().equals(logRecord.getSourceClassName())) {
                    lineNumber = element.getLineNumber();
                    break;
                }
            }

            StringBuilder message = new StringBuilder();
            message.append(threadName).append(" ");
            if (logRecord.getSourceClassName().startsWith(packageName)) {
                message.append(logRecord.getSourceClassName().substring(packageName.length()));
            } else {
                message.append(logRecord.getSourceClassName());
            }
            message
                .append(":").append(lineNumber)
                .append(" - ").append(super.formatMessage(logRecord))
                .append("\n");

            Throwable thrown = logRecord.getThrown();
            if (thrown != null) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                thrown.printStackTrace(printWriter);
                printWriter.flush();
                message.append(stringWriter.toString());
            }

            return message.toString();
        }
    }
}

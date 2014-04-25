package at.pansy.android.logging.helper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static Logger logger = Logger.getLogger(UncaughtExceptionHandler.class.getName());

    private Thread.UncaughtExceptionHandler oldUncaughtExceptionHandler;

    public UncaughtExceptionHandler() {}

    public UncaughtExceptionHandler(Thread.UncaughtExceptionHandler oldUncaughtExceptionHandler) {
        this.oldUncaughtExceptionHandler = oldUncaughtExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        logger.log(Level.SEVERE, throwable.getMessage(), throwable);
        if (oldUncaughtExceptionHandler != null) {
            oldUncaughtExceptionHandler.uncaughtException(thread, throwable);
        }
    }
}

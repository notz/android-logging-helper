package at.pansy.android.logging.helper.demo;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.logging.Level;
import java.util.logging.Logger;

import at.pansy.android.logging.helper.LoggingHelper;


public class MainActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Button shareLogButton = (Button) findViewById(R.id.button);
        shareLogButton.setOnClickListener(view -> LoggingHelper.shareLog(MainActivity.this, null, "This is a logging demo", "This is the optional body text"));

        logSomething();
    }

    private void logSomething() {

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("This is a debug log entry");
        }
        LOGGER.info("This is a info log entry");
        LOGGER.warning("This is a warning");
        LOGGER.severe("This is a error");

        try {
            throw new RuntimeException("This is a runtime exception");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

        LOGGER.log(Level.WARNING, "this is a formatted {0}", new Object[] { "message"});

        LOGGER.fine("This is a 2nd debug log entry");
    }
}

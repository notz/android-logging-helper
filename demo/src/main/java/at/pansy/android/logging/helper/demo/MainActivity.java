package at.pansy.android.logging.helper.demo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.logging.Level;
import java.util.logging.Logger;

import at.pansy.android.logging.helper.LoggingHelper;


public class MainActivity extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Button shareLogButton = (Button) findViewById(R.id.button);
        shareLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoggingHelper.shareLog(MainActivity.this, null, "This is a logging demo");
            }
        });

        logSomething();
    }

    private void logSomething() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("This is a debug log entry");
        }
        logger.info("This is a info log entry");
        logger.warning("This is a warning");
        logger.severe("This is a error");

        try {
            throw new RuntimeException("This is a runtime exception");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        logger.log(Level.WARNING, "this is a formatted {0}", new Object[] { "message"});

        logger.fine("This is a 2nd debug log entry");
    }
}

package at.pansy.android.logging.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFileProvider extends ContentProvider {

    private static final Logger logger = Logger.getLogger(LogFileProvider.class.getName());

    /* package */ static final String EXPECTED_URI_PATH = "/logfile/";

    /* package */ static final String DESTINATION_FILENAME = "logfile.log.gz";

    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
    };

    public LogFileProvider() {
    }

    public static Uri createFileUri(Context context, String tag) {
        return new Uri.Builder()
                .scheme("content")
                .authority(context.getPackageName() + ".logfileprovider")
                .path(EXPECTED_URI_PATH + tag)
                .build();
    }

    public static File getDestinationFile(Context context) {
        return new File(context.getCacheDir(), DESTINATION_FILENAME);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private boolean isValidPath(Uri uri) {
        return uri.getEncodedPath() != null && uri.getEncodedPath().startsWith(EXPECTED_URI_PATH);
    }

    @Override
    public String getType(Uri uri) {
        if (isValidPath(uri)) {
            return "application/x-gzip";
        }

        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!isValidPath(uri)) {
            throw new UnsupportedOperationException("file is unavailable");
        }

        String tag = "logfile";
        List<String> segments = uri.getPathSegments();
        if (segments.size() >= 2) {
            tag = segments.get(1);
        }

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] columns = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;

        for (String column : projection) {
            Object value = null;

            if (OpenableColumns.DISPLAY_NAME.equals(column)) {
                value = tag + ".log.gz";
            } else if (OpenableColumns.SIZE.equals(column)) {
                try {
                    File file = getDestinationFile(getContext());
                    value = file.length();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "failed to read file length", e);
                    value = 0;
                }
            }
            // support for obscure apps, see: https://github.com/commonsguy/cwac-provider#supporting-legacy-apps
            else if (MediaStore.MediaColumns.MIME_TYPE.equals(column)) {
                value = getType(uri);
            } else if (MediaStore.MediaColumns.DATA.equals(column)) {
                value = uri.toString();
            }

            if (value != null) {
                columns[i] = column;
                values[i] = value;
            }

            i++;
        }

        columns = Arrays.copyOf(columns, i);
        values = Arrays.copyOf(values, i);

        MatrixCursor cursor = new MatrixCursor(columns, 1);
        cursor.addRow(values);

        return cursor;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!isValidPath(uri)) {
            throw new UnsupportedOperationException("file is unavailable");
        }

        File file = getDestinationFile(getContext());
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("content provider is read-only");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("content provider is read-only");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("content provider is read-only");
    }
}

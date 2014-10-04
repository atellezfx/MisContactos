package mx.vainiyasoft.agenda.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;

import mx.vainiyasoft.agenda.data.DataChangeTracker;
import mx.vainiyasoft.agenda.entity.ContactoContract;

/**
 * Created by alejandro on 10/3/14.
 */
public class SweeperTask implements Runnable {

    private static final String LOG_TAG = SweeperTask.class.getSimpleName();

    private DataChangeTracker tracker;
    private ContentResolver resolver;
    private Context context;

    public SweeperTask(Activity activity) {
        tracker = new DataChangeTracker(activity);
        resolver = activity.getContentResolver();
        this.context = activity;
    }

    @Override
    public void run() {
        List<String> urisFiltrados = filtrarURIsEnUso(tracker.retrieveImgUriRecords());
        // TODO: Eliminar Log después de fase de pruebas.
        Log.d(LOG_TAG, "URIs a eliminar: " + urisFiltrados);
        for (String imgUri : urisFiltrados) {
            String fileName = Uri.parse(imgUri).getLastPathSegment();
            File imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(imageDir, fileName);
            boolean isDeleted = file.delete();
            // TODO: Eliminar Log después de fase de pruebas.
            Log.d(LOG_TAG, String.format("Archivo: '%s' eliminado? %s", fileName, isDeleted));
        }
    }

    private List<String> filtrarURIsEnUso(List<String> urisRegistrados) {
        for (String imgUri : urisRegistrados) {
            String where = String.format("%s=?", ContactoContract.IMAGEURI);
            String[] whereParams = new String[]{imgUri};
            Cursor cursor = resolver.query(ContactoContract.CONTENT_URI, null, where, whereParams, null);
            if (cursor.getCount() > 0) urisRegistrados.remove(imgUri);
            cursor.close();
        }
        return urisRegistrados;
    }


}

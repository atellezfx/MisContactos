package mx.vainiyasoft.agenda.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by alejandro on 8/30/14.
 */
public class PhotoCopier {

    private static final String LOG_TAG = PhotoCopier.class.getSimpleName();
    private final Context context;

    public PhotoCopier(Activity activity) {
        context = activity;
    }

    public Uri copyImageUri(Uri sourceUri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            File destFile = createFile(sourceUri, resolver);
            AssetFileDescriptor descriptor = resolver.openAssetFileDescriptor(sourceUri, "r");
            FileChannel source = descriptor.createInputStream().getChannel();
            FileChannel destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
                // TODO: Eliminar Logs después de fase de pruebas.
                Log.d(LOG_TAG, "TAMAÑO ORIGEN: " + source.size());
                Log.d(LOG_TAG, "TAMAÑO DESTINO: " + destination.size());
            }
            source.close();
            destination.close();
            return Uri.fromFile(destFile);
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage(), e);
        }
        return null;
    }

    private String obtainFileName(Uri uri, ContentResolver resolver) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        String displayName = cursor.getString(index);
        //TODO: Eliminar Log, luego de fase de pruebas
        Log.d(LOG_TAG, "NOMBRE DE ARCHIVO: " + displayName);
        return displayName;
    }

    private File createFile(String fileName) throws IOException {
        File imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(imageDir, fileName);
    }

    private File createFile(Uri uri, ContentResolver resolver) throws IOException {
        String fileName = obtainFileName(uri, resolver);
        return createFile(fileName);
    }

}

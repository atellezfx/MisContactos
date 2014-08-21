package mx.vainiyasoft.agenda.util;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.DialogInterface.OnClickListener;
import static mx.vainiyasoft.agenda.CrearContactoFragment.LOAD_IMAGE_REQUEST_CODE;
import static mx.vainiyasoft.agenda.CrearContactoFragment.TAKE_PICTURE_REQUEST_CODE;

/**
 * Created by alejandro on 8/21/14.
 */
public class SelectPictureBridge {

    private final String LOG_TAG = SelectPictureBridge.class.getSimpleName();
    private final int OPTION_CAMERA = 0, OPTION_LIBRARY = 1;
    private final Fragment fragment;
    private Uri outputFileUri;

    public SelectPictureBridge(Fragment fragment) {
        this.fragment = fragment;
    }

    public OnClickListener getDialogOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int option) {
                switch (option) {
                    case OPTION_CAMERA:
                        abrirCamara();
                        break;
                    case OPTION_LIBRARY:
                        cargarImagen();
                        break;
                }
            }
        };
    }

    private void cargarImagen() {
        Intent intent = null;
        // Verificamos la versión de la plataforma
        if (Build.VERSION.SDK_INT < 19) {
            // Android JellyBean 4.3 y anteriores
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            // Android KitKat 4.4 o superior
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        fragment.startActivityForResult(intent, LOAD_IMAGE_REQUEST_CODE);
    }

    private void abrirCamara() {
        try {
            File file = createTempFile("agenda", ".jpg");
            outputFileUri = Uri.fromFile(file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            fragment.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage(), e);
        }
    }

    private File createTempFile(String prefijo, String sufijo) throws IOException {
        String timeStamp = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
        StringBuilder builder = new StringBuilder(prefijo).append(timeStamp);
        Context context = fragment.getActivity();
        return File.createTempFile(builder.toString(), sufijo,
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
    }

    public Uri getPictureFileUri() {
        return outputFileUri;
    }

}

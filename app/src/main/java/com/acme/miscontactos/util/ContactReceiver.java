package com.acme.miscontactos.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.acme.miscontactos.entity.Contacto;
import com.acme.miscontactos.entity.ContactoContract;

import java.util.ArrayList;

/**
 * Created by alejandro on 5/2/14.
 */
public class ContactReceiver extends BroadcastReceiver {

    public static final String FILTER_NAME = "listacontactos";
    public static final int CONTACTO_AGREGADO = 1;
    public static final int CONTACTO_ELIMINADO = 2;
    public static final int CONTACTO_ACTUALIZADO = 3;

    private Context context = ApplicationContextProvider.getContext();
    private ContentResolver resolver = context.getContentResolver();

    private final DataChangeTracker tracker;

    public ContactReceiver(Activity activity) {
        tracker = new DataChangeTracker(activity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int operacion = intent.getIntExtra("operacion", -1);
        switch (operacion) {
            case CONTACTO_AGREGADO:
                agregarContacto(intent);
                break;
            case CONTACTO_ELIMINADO:
                eliminarContacto(intent);
                break;
            case CONTACTO_ACTUALIZADO:
                actualizarContacto(intent);
                break;
        }
    }

    private void agregarContacto(Intent intent) {
        Contacto contacto = (Contacto) intent.getParcelableExtra("datos");
        ContentValues values = contacto.getContentValues();
        values.remove(ContactoContract._ID); // Evitar inserción de id en contactos nuevos
        Uri insertedUri = resolver.insert(ContactoContract.CONTENT_URI, values);
        // Obtenemos el id del nuevo registro insertado
        contacto.setId(Integer.parseInt(insertedUri.getLastPathSegment()));
        tracker.recordCreateOp(contacto);
    }

    private void eliminarContacto(Intent intent) {
        ArrayList<Contacto> lista = intent.getParcelableArrayListExtra("datos");
        for (Contacto contacto : lista) {
            Uri queryUri = ContentUris.withAppendedId(ContactoContract.CONTENT_URI, contacto.getId());
            Cursor cursor = resolver.query(queryUri, null, null, null, null, null);
            contacto = Contacto.crearInstanciaDeCursor(cursor);
            int eliminados = resolver.delete(queryUri, null, null);
            Log.d("eliminarContacto?", String.valueOf(eliminados));
            tracker.recordDeleteOp(contacto);
            cursor.close();
        }
    }

    private void actualizarContacto(Intent intent) {
        Contacto contacto = (Contacto) intent.getParcelableExtra("datos");
        ContentValues values = contacto.getContentValues();
        // Evitamos modificar el ID desde este método
        values.remove(ContactoContract._ID);
        Uri updateUri = ContentUris.withAppendedId(ContactoContract.CONTENT_URI, contacto.getId());
        int actualizados = resolver.update(updateUri, values, null, null);
        Log.d("actualizarContacto?", String.valueOf(actualizados));
        // Por el momento la actualización sólo implica el asignar el serverId regresado por
        // el servidor al insertar nuevas contactos, no aplicamos al tracker en esta ocasión
        // tracker.recordUpdateOp(contacto);
    }
}

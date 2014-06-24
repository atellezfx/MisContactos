package com.acme.miscontactos.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
        resolver.insert(ContactoContract.CONTENT_URI, values);
        tracker.recordCreateOp(contacto);
    }

    private void eliminarContacto(Intent intent) {
        ArrayList<Contacto> lista = intent.getParcelableArrayListExtra("datos");
        for (Contacto contacto : lista) {
            tracker.recordDeleteOp(contacto);
            String whereClause = String.format("%s = '%s'", ContactoContract._ID, contacto.getId());
            int eliminados = resolver.delete(ContactoContract.CONTENT_URI, whereClause, null);
            Log.d("eliminarContacto?", String.valueOf(eliminados));
        }
    }

    private void actualizarContacto(Intent intent) {
        Contacto contacto = (Contacto) intent.getParcelableExtra("datos");
        String whereClause = String.format("%s = '%s'", ContactoContract._ID, contacto.getId());
        int actualizados = resolver.update(ContactoContract.CONTENT_URI, contacto.getContentValues(), whereClause, null);
        Log.d("actualizarContacto?", String.valueOf(actualizados));
        // Por el momento la actualización sólo implica el asignar el serverId regresado por
        // el servidor al insertar nuevas contactos, no aplicamos al tracker en esta ocasión
        // tracker.recordUpdateOp(contacto);
    }
}

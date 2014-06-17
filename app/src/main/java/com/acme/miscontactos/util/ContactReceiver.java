package com.acme.miscontactos.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acme.miscontactos.entity.Contacto;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;

/**
 * Created by alejandro on 5/2/14.
 */
public class ContactReceiver extends BroadcastReceiver {

    public static final String FILTER_NAME = "listacontactos";
    public static final int CONTACTO_AGREGADO = 1;
    public static final int CONTACTO_ELIMINADO = 2;
    public static final int CONTACTO_ACTUALIZADO = 3;

    private final OrmLiteBaseActivity<DatabaseHelper> activity;
    private final DataChangeTracker tracker;

    public ContactReceiver(OrmLiteBaseActivity<DatabaseHelper> activity) {
        this.activity = activity;
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
        // TODO: Eliminar Log después de fase de pruebas
        Log.d("Parcelable Contacto", contacto.getNombre());
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getRuntimeExceptionDao(Contacto.class);
            dao.create(contacto);
            tracker.recordCreateOp(contacto);
        }
    }

    private void eliminarContacto(Intent intent) {
        ArrayList<Contacto> lista = intent.getParcelableArrayListExtra("datos");
        // TODO: Eliminar Log después de fase de pruebas
        Log.d("Parcelable List", lista.toString());
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getRuntimeExceptionDao(Contacto.class);
            for (Contacto contacto : lista) {
                dao.refresh(contacto);
                tracker.recordDeleteOp(contacto);
                dao.delete(contacto);
            }
        }
    }

    private void actualizarContacto(Intent intent) {
        Contacto contacto = (Contacto) intent.getParcelableExtra("datos");
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getRuntimeExceptionDao(Contacto.class);
            dao.update(contacto);
            // Por el momento la actualización sólo implica el asignar el serverId regresado por
            // el servidor al insertar nuevas contactos, no aplicamos al tracker en esta ocasión
            // tracker.recordUpdateOp(contacto);
        }
    }
}

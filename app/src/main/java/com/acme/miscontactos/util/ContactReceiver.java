package com.acme.miscontactos.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

    public ContactReceiver(OrmLiteBaseActivity<DatabaseHelper> activity) {
        this.activity = activity;
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
        Contacto contacto = (Contacto) intent.getSerializableExtra("datos");
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getContactoRuntimeDAO();
            dao.create(contacto);
        }
        // Ya no es requerido agregar manualmente al adapter cada contacto, el fragment se inicializa
        // cada vez que se muestra en pantalla, cargando los datos de SQLite
//        adapter.add(contacto);
    }

    private void eliminarContacto(Intent intent) {
        ArrayList<Contacto> lista = (ArrayList<Contacto>) intent.getSerializableExtra("datos");
        // TODO: Corregir eliminación de contactos
        // for (Contacto c : lista) adapter.remove(c);
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getContactoRuntimeDAO();
            dao.delete(lista);
        }
    }

    private void actualizarContacto(Intent intent) {
        Contacto contacto = (Contacto) intent.getSerializableExtra("datos");
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getContactoRuntimeDAO();
            dao.update(contacto);
        }
        // TODO: Corregir edición de contactos
//        int posicion = adapter.getPosition(contacto);
//        adapter.insert(contacto, posicion);
    }
}

package com.acme.miscontactos.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.acme.miscontactos.entity.Contacto;

/**
 * Created by alejandro on 5/30/14.
 */
public class MenuBarActionReceiver extends BroadcastReceiver {

    public static final String FILTER_NAME = "menu_bar_action";
    public static final int ACCION_CONTACTO_AGREGADO = 0;
    public static final int ACCION_ELIMINAR_CONTACTOS = 1;
    public static final int ACCION_SINCRONIZAR_CONTACTOS = 2;
    private final MenuBarActionListener listener;

    public MenuBarActionReceiver(MenuBarActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int operacion = intent.getIntExtra("operacion", -1);
        switch (operacion) {
            case ACCION_CONTACTO_AGREGADO:
                Contacto contacto = intent.getParcelableExtra("datos");
                listener.contactoAgregado(contacto);
                break;
            case ACCION_ELIMINAR_CONTACTOS:
                listener.eliminarContactos();
                break;
            case ACCION_SINCRONIZAR_CONTACTOS:
                listener.sincronizarDatos();
                break;
        }
    }

    public static interface MenuBarActionListener {
        public void contactoAgregado(Contacto contacto);

        public void eliminarContactos();

        public void sincronizarDatos();
    }
}

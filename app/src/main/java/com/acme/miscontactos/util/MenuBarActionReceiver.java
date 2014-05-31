package com.acme.miscontactos.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alejandro on 5/30/14.
 */
public class MenuBarActionReceiver extends BroadcastReceiver {

    public static final String FILTER_NAME = "menu_bar_action";
    public static final int ELIMINAR_CONTACTOS = 1;
    public static final int SINCRONIZAR_CONTACTOS = 2;
    private final MenuBarActionListener listener;

    public MenuBarActionReceiver(MenuBarActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int operacion = intent.getIntExtra("operacion", -1);
        switch (operacion) {
            case ELIMINAR_CONTACTOS:
                listener.eliminarContactos();
                break;
            case SINCRONIZAR_CONTACTOS:
                listener.sincronizarDatos();
                break;
        }
    }

    public static interface MenuBarActionListener {
        public void eliminarContactos();

        public void sincronizarDatos();
    }
}

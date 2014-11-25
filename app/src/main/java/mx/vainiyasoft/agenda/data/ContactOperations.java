package mx.vainiyasoft.agenda.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.ActionMode;

import java.util.HashSet;

/**
 * Created by alejandro on 5/2/14.
 */
public class ContactOperations extends BroadcastReceiver {

    public static final String FILTER_NAME = "contact_operations";

    public static final int ACCION_AGREGAR_CONTACTO = 0;
    public static final int ACCION_ELIMINAR_CONTACTOS = 1;
    public static final int ACCION_ACTUALIZAR_CONTACTO = 2;
    public static final int ACCION_SINCRONIZAR_CONTACTOS = 3;

    private HashSet<ContactOperationsListener> listeners = new HashSet<ContactOperationsListener>();

    public void addContactOperationsListener(ContactOperationsListener listener) {
        listeners.add(listener);
    }

    public void removeContactOperationsListener(ContactOperationsListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int operacion = intent.getIntExtra("operacion", -1);
        switch (operacion) {
            case ACCION_AGREGAR_CONTACTO:
                for (ContactOperationsListener listener : listeners)
                    listener.contactoAgregado(intent);
                break;
            case ACCION_ELIMINAR_CONTACTOS:
                for (ContactOperationsListener listener : listeners)
                    listener.contactoEliminado(intent, null);
                break;
            case ACCION_ACTUALIZAR_CONTACTO:
                for (ContactOperationsListener listener : listeners)
                    listener.contactoActualizado(intent);
                break;
            case ACCION_SINCRONIZAR_CONTACTOS:
                for (ContactOperationsListener listener : listeners)
                    listener.sincronizarContactos();
                break;
        }
    }

    public static interface ContactOperationsListener {
        public void contactoAgregado(Intent intent);

        public void contactoEliminado(Intent intent, ActionMode mode);

        public void contactoActualizado(Intent intent);

        public void sincronizarContactos();
    }

}

package mx.vainiyasoft.agenda.data;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import mx.vainiyasoft.agenda.entity.Contacto;
import mx.vainiyasoft.agenda.entity.ContactoContract;
import mx.vainiyasoft.agenda.util.ApplicationContextProvider;
import mx.vainiyasoft.agenda.widgets.ContadorContactosWidget;

/**
 * Created by alejandro on 11/25/14.
 */
public class ContactUtilities {

    private final DataChangeTracker tracker;
    private Context context = ApplicationContextProvider.getContext();
    private ContentResolver resolver = context.getContentResolver();

    public ContactUtilities(Activity activity) {
        tracker = new DataChangeTracker(activity);
    }

    private Contacto agregarContacto(Intent intent) {
        Contacto contacto = (Contacto) intent.getParcelableExtra("datos");
        ContentValues values = contacto.getContentValues();
        values.remove(ContactoContract._ID); // Evitar inserción de id en contactos nuevos
        Uri insertedUri = resolver.insert(ContactoContract.CONTENT_URI, values);
        // Obtenemos el id del nuevo registro insertado
        contacto.setId(Integer.parseInt(insertedUri.getLastPathSegment()));
        notificarWidgetPorDatosModificados();
        tracker.recordCreateOp(contacto);
        return contacto;
    }

    private void eliminarContacto(Intent intent) {
        ArrayList<Contacto> lista = intent.getParcelableArrayListExtra("datos");
        for (Contacto contacto : lista) {
            Uri queryUri = ContentUris.withAppendedId(ContactoContract.CONTENT_URI, contacto.getId());
            Cursor cursor = resolver.query(queryUri, null, null, null, null, null);
            contacto = Contacto.crearInstanciaDeCursor(cursor);
            int eliminados = resolver.delete(queryUri, null, null);
            // TODO: Eliminar este Log al terminar fase de desarrollo
            Log.d("eliminarContacto?", String.valueOf(eliminados));
            // No podemos eliminar directamente el URI de la imagen del contacto, pues es posible
            // que algún otro contacto esté utilizando el mismo URI, sólo lo registramos para
            // revisarlo más tarde con una tarea programada
            tracker.recordImgUri(contacto.getImageUri());
            tracker.recordDeleteOp(contacto);
            cursor.close();
        }
        notificarWidgetPorDatosModificados();
    }

    private void notificarWidgetPorDatosModificados() {
        ComponentName cname = new ComponentName(context, ContadorContactosWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        // Obtenemos los IDs de nuestro widget, ya que puede haber mas de una instancia en pantallas
        int[] widgetIds = manager.getAppWidgetIds(cname);
        // TODO: Eliminar este Log al terminar fase de desarrollo
        Log.d("Widgets IDs:", Arrays.toString(widgetIds));
        ContadorContactosWidget.updateAppWidget(context, manager, widgetIds);
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

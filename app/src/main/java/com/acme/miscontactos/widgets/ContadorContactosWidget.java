package com.acme.miscontactos.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.acme.miscontactos.R;
import com.acme.miscontactos.entity.Contacto;
import com.acme.miscontactos.entity.ContactoContract;

import java.util.List;


/**
 * Implementation of App Widget functionality.
 */
public class ContadorContactosWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.contador_contactos_widget);
        // Invocamos el ContentProvider de Contacto para obtener el número de registros almacenados
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ContactoContract.CONTENT_URI, null, null, null, null);
        List<Contacto> contactos = Contacto.crearListaDeCursor(cursor);
        views.setTextViewText(R.id.widget_simple_texto, String.valueOf(contactos.size()));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        cursor.close();
    }
}



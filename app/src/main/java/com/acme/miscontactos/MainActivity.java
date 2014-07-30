package com.acme.miscontactos;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.acme.miscontactos.net.HttpServiceBroker;
import com.acme.miscontactos.util.ContactReceiver;
import com.acme.miscontactos.util.MenuBarActionReceiver;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTouch;

import static android.gesture.GestureOverlayView.OnGesturePerformedListener;

// Ya no se usa el OrmLiteBaseActivity al usar ContentProvider
public class MainActivity extends Activity implements OnGesturePerformedListener {

    @InjectView(R.id.btn_crear_contacto)
    protected ImageButton btnCrearContacto;

    @InjectView(R.id.btn_lista_contactos)
    protected ImageButton btnListaContactos;

    @InjectView(R.id.btn_eliminar_contactos)
    protected ImageButton btnEliminarContactos;

    @InjectView(R.id.btn_sincronizar)
    protected ImageButton btnSincronizar;

    private CrearContactoFragment fragmentoCrear;
    private ListaContactosFragment fragmentoLista;
    private GestureLibrary gestureLib;
    private final int CONFIG_REQUEST_CODE = 0;
    private ContactReceiver receiver;
    private HttpServiceBroker broker;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View overlayView = inicializarVista();
        setContentView(overlayView);
        ButterKnife.inject(this);
        inicializaActionBar();
        inicializaComponentes();
    }

    private View inicializarVista() {
        GestureOverlayView overlay = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_main, null);
        overlay.addView(inflate);
        overlay.addOnGesturePerformedListener(this);
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        gestureLib.load();
        return overlay;
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new ContactReceiver(this);
        broker = new HttpServiceBroker();
        registerReceiver(receiver, new IntentFilter(ContactReceiver.FILTER_NAME));
        registerReceiver(broker, new IntentFilter(HttpServiceBroker.FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unregisterReceiver(broker);
    }

    private void inicializaComponentes() {
        View view = findViewById(R.id.rootPane);
        String viewTag = String.valueOf(view.getTag());
        // Verdadero para los TAGs "phone" y "phone_landscape"
        if (viewTag.startsWith("phone")) cargarFragmento(getFragmentoLista());
    }

    private void inicializaActionBar() {
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
    }

    //<editor-fold desc="METODOS GET DE INICIALIZACION BAJO DEMANDA (LAZY INITIALIZATION)">
    public CrearContactoFragment getFragmentoCrear() {
        if (fragmentoCrear == null) fragmentoCrear = new CrearContactoFragment();
        return fragmentoCrear;
    }

    public ListaContactosFragment getFragmentoLista() {
        if (fragmentoLista == null) fragmentoLista = new ListaContactosFragment();
        return fragmentoLista;
    }
    //</editor-fold>

    private void cargarFragmento(Fragment fragmento) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.contenedor, fragmento);
        transaction.commit();
    }

    @OnTouch({R.id.btn_crear_contacto, R.id.btn_lista_contactos, R.id.btn_eliminar_contactos, R.id.btn_sincronizar})
    public boolean onTouch(View view, MotionEvent evt) {
        ImageButton btn = (ImageButton) view;
        int actionMasked = evt.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                btn.setColorFilter(R.color.entintado_oscuro);
                btn.invalidate();
                cambiarFragmento(btn);
                break;
            case MotionEvent.ACTION_UP:
                btn.clearColorFilter();
                btn.invalidate();
                break;
        }
        return true;
    }

    private void cambiarFragmento(View view) {
        switch (view.getId()) {
            case R.id.btn_crear_contacto:
                cargarFragmento(getFragmentoCrear());
                break;
            case R.id.btn_lista_contactos:
                cargarFragmento(getFragmentoLista());
                break;
            case R.id.btn_eliminar_contactos:
                notificarEliminarContactos();
                break;
            case R.id.btn_sincronizar:
                notificarSincronizacion();
                break;
        }
    }

    @Override
    public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
        for (Prediction pred : predictions) {
            if (pred.score > 1.0) {
                if (pred.name.equals("eliminar")) notificarEliminarContactos();
                else if (pred.name.equals("sincronizar")) notificarSincronizacion();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Sólo existe una opción en el menú
        Intent intent = new Intent();
        intent.setClass(this, ConfiguracionActivity.class);
        startActivityForResult(intent, CONFIG_REQUEST_CODE);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIG_REQUEST_CODE) {
            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(this);
            String username = shp.getString("username", null);
            String mesg = i18n(R.string.mesg_preferences_saved, username);
            Toast.makeText(this, mesg, Toast.LENGTH_SHORT).show();
        }
    }

    private void notificarSincronizacion() {
        Intent intent = new Intent(MenuBarActionReceiver.FILTER_NAME);
        intent.putExtra("operacion", MenuBarActionReceiver.ACCION_SINCRONIZAR_CONTACTOS);
        sendBroadcast(intent);
    }

    private void notificarEliminarContactos() {
        Intent intent = new Intent(MenuBarActionReceiver.FILTER_NAME);
        intent.putExtra("operacion", MenuBarActionReceiver.ACCION_ELIMINAR_CONTACTOS);
        sendBroadcast(intent);
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Desactivar la autodetección y obligar al uso de atributos y no de getter/setter
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    private String i18n(int resourceId, Object... formatArgs) {
        return getResources().getString(resourceId, formatArgs);
    }

}

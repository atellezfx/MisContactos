package com.acme.miscontactos;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.acme.miscontactos.util.DatabaseHelper;
import com.acme.miscontactos.util.MenuBarActionReceiver;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> implements View.OnTouchListener {

    private ImageButton btnCrearContacto, btnListaContactos, btnEliminarContactos, btnSincronizar;
    private CrearContactoFragment fragmentoCrear;
    private ListaContactosFragment fragmentoLista;
    private final int CONFIG_REQUEST_CODE = 0;
    private ContactReceiver receiver;
    private HttpServiceBroker broker;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializaActionBar();
        inicializaComponentes();
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
        btnCrearContacto = (ImageButton) findViewById(R.id.btn_crear_contacto);
        btnCrearContacto.setOnTouchListener(this);
        btnListaContactos = (ImageButton) findViewById(R.id.btn_lista_contactos);
        btnListaContactos.setOnTouchListener(this);
        btnEliminarContactos = (ImageButton) findViewById(R.id.btn_eliminar_contactos);
        btnEliminarContactos.setOnTouchListener(this);
        btnSincronizar = (ImageButton) findViewById(R.id.btn_sincronizar);
        btnSincronizar.setOnTouchListener(this);
        cargarFragmento(getFragmentoLista());
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

    @Override
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
            String mesg = String.format("Datos del usuario '%s' guardados", username);
            Toast.makeText(this, mesg, Toast.LENGTH_SHORT).show();
        }
    }

    private void notificarSincronizacion() {
        Intent intent = new Intent(MenuBarActionReceiver.FILTER_NAME);
        intent.putExtra("operacion", MenuBarActionReceiver.SINCRONIZAR_CONTACTOS);
        sendBroadcast(intent);
    }

    private void notificarEliminarContactos() {
        Intent intent = new Intent(MenuBarActionReceiver.FILTER_NAME);
        intent.putExtra("operacion", MenuBarActionReceiver.ELIMINAR_CONTACTOS);
        sendBroadcast(intent);
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Desactivar la autodetección y obligar al uso de atributos y no de getter/setter
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }


}

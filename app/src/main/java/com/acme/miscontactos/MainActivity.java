package com.acme.miscontactos;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.acme.miscontactos.util.ContactReceiver;
import com.acme.miscontactos.util.DatabaseHelper;
import com.acme.miscontactos.util.MenuBarActionReceiver;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> implements View.OnTouchListener {

    private ImageButton btnCrearContacto, btnListaContactos, btnEliminarContactos, btnSincronizar;
    private CrearContactoFragment fragmentoCrear;
    private ListaContactosFragment fragmentoLista;
    private ContactReceiver receiver;
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
        registerReceiver(receiver, new IntentFilter(ContactReceiver.FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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


}

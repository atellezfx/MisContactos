package mx.vainiyasoft.agenda;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import mx.vainiyasoft.agenda.data.ContactReceiver;
import mx.vainiyasoft.agenda.nav.DrawerAdapter;
import mx.vainiyasoft.agenda.net.HttpServiceBroker;
import mx.vainiyasoft.agenda.util.MenuBarActionReceiver;
import mx.vainiyasoft.agenda.util.SweeperTask;

import static android.gesture.GestureOverlayView.OnGesturePerformedListener;

// Ya no se usa el OrmLiteBaseActivity al usar ContentProvider
public class MainActivity extends Activity implements OnGesturePerformedListener {

    @InjectView(R.id.rootPane)
    protected DrawerLayout drawerLayout;
    @InjectView(R.id.nav_drawer)
    protected ListView drawerList;

    private ActionBarDrawerToggle drawerToggle;
    private CharSequence mDrawerTitle, mTitle;
    private String[] titulos;

    private CrearContactoFragment fragmentoCrear;
    private ListaContactosFragment fragmentoLista;
    private GestureLibrary gestureLib;
    private final int CONFIG_REQUEST_CODE = 0;
    private ContactReceiver receiver;
    private HttpServiceBroker broker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View overlayView = inicializarVista();
        // Primera técnica de apps a pantalla completa "FullScreen Activity"
//        setFullScreen(this);  // Es necesario realizarlo antes del setContentView()
        setContentView(overlayView);
        titulos = getResources().getStringArray(R.array.nav_drawer_titles);
        ButterKnife.inject(this);
        DrawerAdapter adapter = new DrawerAdapter(this, R.layout.drawer_item, titulos);
        drawerList.setAdapter(adapter);
        inicializarNavigationDrawer();
        inicializaComponentes();
        requestBackup();
        // Segunda técnica, utilizando el modo immersivo no pegadizo
        // modoImmersivoNoPegadizo(); // No se obtiene un resultado óptimo.
    }

    // Tercera opción y la más "elegante", Modo Immersivo Pegadizo
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            drawerLayout.setSystemUiVisibility(
                    // Igual, la siguiente línea oculta el ActionBar, la comentamos
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    // Las siguientes dos líneas ocultan la barra de navegación, en nuestra app
                    // puede traer problemas, pues sí resulta últi. Las comentamos también.
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

//    private void modoImmersivoNoPegadizo() {
//        drawerLayout.setSystemUiVisibility(
//                // Algunos blogs y documentación recomiendan utilizar la siguiente línea de código
//                // pero igualmente la comentamos, pues oculta el ActionBar y no aplica en nuestra app
////                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
//                        View.SYSTEM_UI_FLAG_IMMERSIVE
//        );
//    }

//    private void setFullScreen(Activity activity) {
//        // Algunos blogs sugieren la siguiente línea de código pero en nuestro caso nos quitará el
//        // ActionBar, así que la comentaremos para este caso
//        // activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Window win = activity.getWindow();
//        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    }

    private void requestBackup() {
        BackupManager manager = new BackupManager(this);
        manager.dataChanged();
    }

    private void inicializarNavigationDrawer() {
        mTitle = mDrawerTitle = getTitle();
        final ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_navigation_drawer,
                    R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    actionBar.setTitle(mDrawerTitle);
                    invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    actionBar.setTitle(mTitle);
                    invalidateOptionsMenu();
                }
            };
            // Asignamos el objeto drawerToggle como el DrawerListener
            drawerLayout.setDrawerListener(drawerToggle);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sincronizamos el estado del drawerToggle después de que se ejecute el método onRestoreInstanceState
        if(drawerToggle!=null) drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(drawerToggle!=null) drawerToggle.onConfigurationChanged(newConfig);
    }

    private View inicializarVista() {
        GestureOverlayView overlay = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_main, null);
        overlay.addView(inflate);
        overlay.addOnGesturePerformedListener(this);
        overlay.setGestureVisible(false);
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

    @Override
    protected void onStop() {
        Handler handler = new Handler();
        SweeperTask task = new SweeperTask(this);
        handler.post(task);
        super.onStop();
    }

    private void inicializaComponentes() {
        View view = findViewById(R.id.rootPane);
        String viewTag = String.valueOf(view.getTag());
        // Verdadero para los TAGs "phone" y "phone_landscape"
        if (viewTag.startsWith("phone")) cargarFragmento(getFragmentoLista());
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
        FragmentTransaction ft = manager.beginTransaction();
        // Asignamos el valor de transition para disparar la animación correcta
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.contenedor, fragmento);
        // Agregamos esta transacción al "back stack". Esto significa que la transacción
        // será recordada después de que haya sido aceptada (commit), y podrá ser
        // ejecutada en reversa después cuando se obtenga del stack.
        ft.addToBackStack("pantallas");
        ft.commit();
    }

    @OnItemClick(R.id.nav_drawer)
    public void selectItem(int position) {
        switch (position) {
            case 0:
                cargarFragmento(getFragmentoCrear());
                setTitle(titulos[position]);
                break;
            case 1:
                cargarFragmento(getFragmentoLista());
                setTitle(titulos[position]);
                break;
            case 2:
                notificarEliminarContactos();
                break;
            case 3:
                notificarSincronizacion();
                break;
        }
        // Resaltar el elemento seleciconado, actualizar el títulos y cerrar el drawer
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(title);
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
        // Importante atrapar primero las notificaciones del drawer
        if (drawerToggle.onOptionsItemSelected(item)) return true;
        // Sólo existe una opción en el menú
        Intent intent = new Intent(this, ConfiguracionActivity.class);
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

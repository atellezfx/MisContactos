package mx.vainiyasoft.agenda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mx.vainiyasoft.agenda.data.ContactArrayAdapter;
import mx.vainiyasoft.agenda.data.ContactOperations;
import mx.vainiyasoft.agenda.data.ContactUtilities;
import mx.vainiyasoft.agenda.entity.Contacto;
import mx.vainiyasoft.agenda.entity.ContactoContract;
import mx.vainiyasoft.agenda.net.NetworkBridge;
import mx.vainiyasoft.agenda.util.ShareOptionsBridge;

import static android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static android.widget.AdapterView.OnItemLongClickListener;
import static mx.vainiyasoft.agenda.data.ContactOperations.ContactOperationsListener;

// Si por alguna razón, al compilar el proyecto marca que no encuentra la clase ContactoContract
// será necesario ir al menú Build > Make Module 'app' o bien volver a compilar el proyecto,
// en AdroidStudio RC1 realizaron cambios en el modelo de compilación de proyectos, que afecto
// nuestro procesador de anotaciones.

/**
 * Created by alejandro on 5/2/14.
 */
public class ListaContactosFragment extends ListFragment
        implements ContactOperationsListener, OnRefreshListener, MultiChoiceModeListener, OnItemLongClickListener {

    private static final String LOG_TAG = ListaContactosFragment.class.getSimpleName();

    private ListView listView;
    private SwipeRefreshLayout refreshLayout;
    private ContactUtilities utils;
    private ContactArrayAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Activity activity = getActivity();
            utils = new ContactUtilities(activity);
            ContentResolver resolver = activity.getContentResolver();
            Cursor cursor = resolver.query(ContactoContract.CONTENT_URI, null, null, null, null);
            List<Contacto> contactos = Contacto.crearListaDeCursor(cursor);
            // El adapter será el encargado de ir creando los fragmentos conforme se necesiten y "reciclarlos"
            listAdapter = new ContactArrayAdapter(activity, R.layout.listview_item, contactos);
            setListAdapter(listAdapter);
            cursor.close();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true); // Habilita el ActionBAr de este fragment para tener botones
        SharedPreferences prefs = loadPreferences();
        boolean swipeEnabled = prefs.getBoolean("swipe_refresh", false);
        if (swipeEnabled) {
            // Creamos una isntancia de SwipeRefreshLayout que contendrá la vista del fragmento actual
            refreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());
            // Agreguemos la lista del ragmento para qe ocupe el tamaño completo del cotnenedor
            refreshLayout.addView(listFragmentView, MATCH_PARENT, MATCH_PARENT);
            // Ahora el control SwipeRefreshLayout ocupará el espacio disponible en pantalla
            refreshLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            refreshLayout.setOnRefreshListener(this);
            return refreshLayout;
        }
        return listFragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); // Cambiar a CHOICE_MODE_MULTIPLE_MODAL al utilizar Contextual ActionBAr (CAB)
        listView.setMultiChoiceModeListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setDividerHeight(5); // Usabamos 5dp en el xml
        listView.setBackgroundColor(0xFFD1D1D1);
        listView.setDivider(null); // Remover las separaciones entre elementos de la lista
        // Puliendo la app, ocultamos el teclado virtual cuando no se está utilizando
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onRefresh() {
        if (refreshLayout != null) refreshLayout.setRefreshing(true);
        Activity activity = getActivity();
        if (isAppConfigured()) {
            if (NetworkBridge.isWifiEnabled(activity)) {
                NetworkBridge bridge = new NetworkBridge(activity);
                bridge.sincronizarDatos();
            } else {
                Toast.makeText(activity, i18n(R.string.mesg_wifi_not_available),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, i18n(R.string.mesg_app_not_configured),
                    Toast.LENGTH_SHORT).show();
        }
        if (refreshLayout != null) refreshLayout.setRefreshing(false);
    }

    //<editor-fold desc="METODOS DE ADMINISTRACION DE CONTACTOS">
    @Override
    public void contactoAgregado(Intent intent) {
        Contacto contacto = utils.agregarContacto(intent);
        listAdapter.add(contacto);
    }

    @Override
    public void contactoEliminado(Intent intent, final ActionMode actionMode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_action_warning);
        builder.setTitle(i18n(R.string.title_alertdialog_confirm));
        builder.setMessage(i18n(R.string.mesg_confirm_delete));
        builder.setPositiveButton(i18n(R.string.mesg_positive_dialog_option), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<Contacto> seleccion = new ArrayList<Contacto>();
                for (int position : listAdapter.getCurrentCheckedPositions()) {
                    if (listAdapter.isPositionChecked(position))
                        seleccion.add(listAdapter.getItem(position));
                }
                for (Contacto con : seleccion) listAdapter.remove(con);
                Intent intent = new Intent(ContactOperations.FILTER_NAME);
                intent.putExtra("operacion", ContactOperations.ACCION_ELIMINAR_CONTACTOS);
                intent.putParcelableArrayListExtra("datos", seleccion);
                utils.eliminarContacto(intent);
                if (actionMode != null) actionMode.finish();
                listAdapter.clearSelection();
                seleccionados = 0;
            }
        });
        builder.setNegativeButton(i18n(R.string.mesg_negative_dialog_option), null);
        builder.show();
    }

    @Override
    public void contactoActualizado(Intent intent) {
        utils.actualizarContacto(intent);
    }

    @Override
    public void sincronizarContactos() {
        onRefresh();
    }
    //</editor-fold>

    private String i18n(int resourceId, Object... formatArgs) {
        return getResources().getString(resourceId, formatArgs);
    }

    private SharedPreferences loadPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    private boolean isAppConfigured() {
        SharedPreferences prefs = loadPreferences();
        boolean configured = prefs.contains("username");
        configured &= prefs.contains("swipe_refresh");
        configured &= prefs.contains("server_address");
        return configured && prefs.contains("server_port");
    }

    //<editor-fold desc="METODOS Y RECURSOS PARA ACTION BAR CONTEXTUAL">
    private final int CONFIG_REQUEST_CODE = 101;
    private int seleccionados = 0;

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
        if (checked) {
            seleccionados++;
            listAdapter.setNewSelection(position, checked);
        } else {
            seleccionados--;
            listAdapter.removeSelection(position);
        }
        Activity activity = getActivity();
        View shareItem = activity.findViewById(R.id.item_action_share);
        shareItem.setVisibility(seleccionados == 1 ? View.VISIBLE : View.INVISIBLE);
        actionMode.setTitle(String.format("%d selected", seleccionados));
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        seleccionados = 0;
        Activity activity = getActivity();
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_action_remove:
                this.contactoEliminado(null, actionMode);
                break;
            case R.id.item_action_share:
                Set<Integer> checked = listAdapter.getCurrentCheckedPositions();
                if (checked.size() == 1) { // Sólo habilitamos compartir cuando un elemento está seleccioando por el momento
                    ShareOptionsBridge bridge = new ShareOptionsBridge(listAdapter, getActivity());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.share_title);
                    Integer position = (Integer) checked.toArray()[0];
                    builder.setItems(R.array.share_options, bridge.getDialogOnClickListener(position));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        listAdapter.clearSelection();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIG_REQUEST_CODE) {
            Activity activity = getActivity();
            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(activity);
            String username = shp.getString("username", null);
            String mesg = i18n(R.string.mesg_preferences_saved, username);
            Toast.makeText(activity, mesg, Toast.LENGTH_SHORT).show();
        }
    }
    //</editor-fold>

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        listView.setItemChecked(position, !listAdapter.isPositionChecked(position));
        return false;
    }

    /**
     * Creamos una clase hijo de SwipeRefreshLayout debido a que la clase padre
     * sólo soporta un contenedor, el cual espera que sea el que dispare los eventos
     * de deslizar (swipe). En nuestro caso el layout que usamos contiene una vista
     * hijio de tipo ViewGroup.
     * <p/>
     * Para activar el soporte a "swipe-to-refresh" necesitamos sobreescribir los
     * métodos adecuados para que reaccionen sobre el ListView contenido en el fragmento
     */
    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {
        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        @Override
        public boolean canChildScrollUp() {
            if (listView.getVisibility() == View.VISIBLE)
                return canListViewScrollUp(listView);
            else return false;
        }

        /**
         * Es necesario verificar la versión del sistema, para proporciona compatibilidad
         * con versiones anteriores,
         *
         * @param listView
         * @return
         */
        private boolean canListViewScrollUp(ListView listView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                return ViewCompat.canScrollVertically(listView, -1);
            } else {
                return listView.getChildCount() > 0 &&
                        (listView.getFirstVisiblePosition() > 0 ||
                                listView.getChildAt(0).getTop() < listView.getPaddingTop());
            }
        }
    }


}

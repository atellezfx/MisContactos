package mx.vainiyasoft.agenda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mx.vainiyasoft.agenda.data.ContactArrayAdapter;
import mx.vainiyasoft.agenda.data.ContactReceiver;
import mx.vainiyasoft.agenda.entity.Contacto;
import mx.vainiyasoft.agenda.entity.ContactoContract;
import mx.vainiyasoft.agenda.net.NetworkBridge;
import mx.vainiyasoft.agenda.util.MenuBarActionReceiver;

import static android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static mx.vainiyasoft.agenda.util.MenuBarActionReceiver.FILTER_NAME;
import static mx.vainiyasoft.agenda.util.MenuBarActionReceiver.MenuBarActionListener;

/**
 * Created by alejandro on 5/2/14.
 */
public class ListaContactosFragment extends ListFragment implements MenuBarActionListener, OnRefreshListener {

    private static final String LOG_TAG = ListaContactosFragment.class.getSimpleName();

    private ListView listView;
    private SwipeRefreshLayout refreshLayout;

    private MenuBarActionReceiver receiver;
    private ContactArrayAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Context context = getActivity();
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(ContactoContract.CONTENT_URI, null, null, null, null);
            List<Contacto> contactos = Contacto.crearListaDeCursor(cursor);
            // El adapter será el encargado de ir creando los fragmentos conforme se necesiten y "reciclarlos"
            listAdapter = new ContactArrayAdapter(context, R.layout.listview_item, contactos);
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
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // Cambiar a CHOICE_MODE_MULTIPLE_MODAL al utilizar Contextual ActionBAr (CAB)
        listView.setDividerHeight(5); // Usabamos 5dp en el xml
        listView.setBackgroundColor(0xFFD1D1D1);
        listView.setDivider(null); // Remover las separaciones entre elementos de la lista
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new MenuBarActionReceiver(this);
        // Sólo recibirá notificaciones mientras se encuentre mostrando en pantalla
        getActivity().registerReceiver(receiver, new IntentFilter(FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onRefresh() {
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
        refreshLayout.setRefreshing(false);
    }

    //<editor-fold desc="METODOS DE ADMINISTRACION DE CONTACTOS">
    @Override
    public void contactoAgregado(Contacto contacto) {
        listAdapter.add(contacto);
    }

    @Override
    public void eliminarContactos() {
        String mensaje = "¿Está seguro de eliminar los contactos seleccionados?";
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
                Intent intent = new Intent(ContactReceiver.FILTER_NAME);
                intent.putExtra("operacion", ContactReceiver.CONTACTO_ELIMINADO);
                intent.putParcelableArrayListExtra("datos", seleccion);
                getActivity().sendBroadcast(intent);
                listView.clearChoices();
            }
        });
        builder.setNegativeButton(i18n(R.string.mesg_negative_dialog_option), null);
        builder.show();
    }

    @Override
    public void sincronizarDatos() {
        NetworkBridge bridge = new NetworkBridge(getActivity());
        bridge.sincronizarDatos();
    }
    //</editor-fold>

    // Comentamos este método, pues lo utilizaremos más adelante.
//    @OnItemLongClick(R.id.fragment_listview)
//    public boolean onItemLongClick(int position) {
//        ShareOptionsBridge bridge = new ShareOptionsBridge(listAdapter, getActivity());
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.share_title);
//        builder.setItems(R.array.share_options, bridge.getDialogOnClickListener(position));
//        AlertDialog dialog = builder.create();
//        dialog.show();
//        return true;
//    }

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

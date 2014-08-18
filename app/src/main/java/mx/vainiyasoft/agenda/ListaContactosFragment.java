package mx.vainiyasoft.agenda;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemLongClick;
import mx.vainiyasoft.agenda.entity.Contacto;
import mx.vainiyasoft.agenda.entity.ContactoContract;
import mx.vainiyasoft.agenda.entity.JSONBean;
import mx.vainiyasoft.agenda.net.HttpServiceBroker;
import mx.vainiyasoft.agenda.util.AsyncTaskListener;
import mx.vainiyasoft.agenda.util.ContactArrayAdapter;
import mx.vainiyasoft.agenda.util.ContactReceiver;
import mx.vainiyasoft.agenda.util.DataChangeTracker;
import mx.vainiyasoft.agenda.util.MenuBarActionReceiver;
import mx.vainiyasoft.agenda.util.ShareOptionsBridge;

/**
 * Created by alejandro on 5/2/14.
 */
public class ListaContactosFragment extends Fragment
        implements MenuBarActionReceiver.MenuBarActionListener, AsyncTaskListener<List<String>> {

    private static final String LOG_TAG = ListaContactosFragment.class.getSimpleName();

    @InjectView(R.id.fragment_listview)
    protected ListView contactsListView;

    private MenuBarActionReceiver receiver;
    private ContactArrayAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lista_contactos, container, false);
        ButterKnife.inject(this, rootView);
        setHasOptionsMenu(true); // Habilita el ActionBAr de este fragment para tener botones
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        inicializarComponentes(getActivity(), savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new MenuBarActionReceiver(this);
        // Sólo recibirá notificaciones mientras se encuentre mostrando en pantalla
        getActivity().registerReceiver(receiver, new IntentFilter(MenuBarActionReceiver.FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private void inicializarComponentes(Context context, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(ContactoContract.CONTENT_URI, null, null, null, null);
            List<Contacto> contactos = Contacto.crearListaDeCursor(cursor);
            // El adapter será el encargado de ir creado los fragmentos conforme se necesiten, y "reciclando"
            // los recursos (layouts) cuando sea necesario
            listAdapter = new ContactArrayAdapter(context, R.layout.listview_item, contactos);
            contactsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            contactsListView.setAdapter(listAdapter);
            cursor.close();
        }
    }

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
                SparseBooleanArray array = contactsListView.getCheckedItemPositions();
                ArrayList<Contacto> seleccion = new ArrayList<Contacto>();
                for (int j = 0; j < array.size(); j++) {
                    // Posición del contacto en el adaptador
                    int posicion = array.keyAt(j);
                    if (array.valueAt(j)) seleccion.add(listAdapter.getItem(posicion));
                }
                for (Contacto con : seleccion) listAdapter.remove(con);
                Intent intent = new Intent(ContactReceiver.FILTER_NAME);
                intent.putExtra("operacion", ContactReceiver.CONTACTO_ELIMINADO);
                intent.putParcelableArrayListExtra("datos", seleccion);
                getActivity().sendBroadcast(intent);
                contactsListView.clearChoices();
            }
        });
        builder.setNegativeButton(i18n(R.string.mesg_negative_dialog_option), null);
        builder.show();
    }

    @Override
    public void sincronizarDatos() {
        DataChangeTracker tracker = new DataChangeTracker(getActivity());
        ArrayList<DataChangeTracker.StoredRecord> allRecords = tracker.retrieveRecords();
        ArrayList<DataChangeTracker.StoredRecord> createList = new ArrayList<DataChangeTracker.StoredRecord>();
        ArrayList<DataChangeTracker.StoredRecord> deleteList = new ArrayList<DataChangeTracker.StoredRecord>();
        ArrayList<DataChangeTracker.StoredRecord> updateList = new ArrayList<DataChangeTracker.StoredRecord>();
        for (DataChangeTracker.StoredRecord record : allRecords) {
            switch (record.getType()) {
                case DataChangeTracker.StoredRecord.TYPE_CREATE:
                    createList.add(record);
                    break;
                case DataChangeTracker.StoredRecord.TYPE_DELETE:
                    deleteList.add(record);
                    break;
                case DataChangeTracker.StoredRecord.TYPE_UPDATE:
                    updateList.add(record);
                    break;
            }
        }
        doPost(createList);
        doPut(updateList);
        doDelete(deleteList);
        tracker.clearRecords();
    }

    @OnItemLongClick(R.id.fragment_listview)
    public boolean onItemLongClick(int position) {
        ShareOptionsBridge bridge = new ShareOptionsBridge(listAdapter, getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.share_title);
        builder.setItems(R.array.share_options, bridge.getDialogOnClickListener(position));
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private void doDelete(ArrayList<DataChangeTracker.StoredRecord> deleteList) {
        Intent intent = new Intent(HttpServiceBroker.FILTER_NAME);
        intent.putExtra("metodo_http", HttpServiceBroker.HTTP_DELETE_METHOD);
        ArrayList<JSONBean> datos = new ArrayList<JSONBean>();
        for (DataChangeTracker.StoredRecord record : deleteList) datos.add(record.getData());
        intent.putParcelableArrayListExtra("datos", datos);
        getActivity().sendBroadcast(intent);
    }

    private void doPut(ArrayList<DataChangeTracker.StoredRecord> updateList) {
        Intent intent = new Intent(HttpServiceBroker.FILTER_NAME);
        intent.putExtra("metodo_http", HttpServiceBroker.HTTP_PUT_METHOD);
        ArrayList<JSONBean> datos = new ArrayList<JSONBean>();
        for (DataChangeTracker.StoredRecord record : updateList) datos.add(record.getData());
        intent.putParcelableArrayListExtra("datos", datos);
        getActivity().sendBroadcast(intent);
    }

    private void doPost(ArrayList<DataChangeTracker.StoredRecord> createList) {
        Intent intent = new Intent(HttpServiceBroker.FILTER_NAME);
        intent.putExtra("metodo_http", HttpServiceBroker.HTTP_POST_METHOD);
        ArrayList<JSONBean> datos = new ArrayList<JSONBean>();
        for (DataChangeTracker.StoredRecord record : createList) datos.add(record.getData());
        intent.putParcelableArrayListExtra("datos", datos);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void processResult(List<String> result) {
        for (String cad : result)
            Toast.makeText(getActivity(), i18n(R.string.mesg_toast_sync_confirm, cad),
                    Toast.LENGTH_SHORT).show();
    }

    private String i18n(int resourceId, Object... formatArgs) {
        return getResources().getString(resourceId, formatArgs);
    }

}

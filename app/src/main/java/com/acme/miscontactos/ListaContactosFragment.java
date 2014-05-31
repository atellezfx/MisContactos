package com.acme.miscontactos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.acme.miscontactos.entity.Contacto;
import com.acme.miscontactos.util.ContactListAdapter;
import com.acme.miscontactos.util.ContactReceiver;
import com.acme.miscontactos.util.DatabaseHelper;
import com.acme.miscontactos.util.MenuBarActionReceiver;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;

import static com.acme.miscontactos.util.MenuBarActionReceiver.MenuBarActionListener;

/**
 * Created by alejandro on 5/2/14.
 */
public class ListaContactosFragment extends Fragment implements MenuBarActionListener {

    private ArrayAdapter<Contacto> adapter;
    private MenuBarActionReceiver receiver;
    private ListView contactsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lista_contactos, container, false);
        inicializarComponentes(rootView);
        setHasOptionsMenu(true); // Habilita el ActionBAr de este fragment para tener botones
        return rootView;
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

    private void inicializarComponentes(View view) {
        contactsListView = (ListView) view.findViewById(R.id.listView);
        adapter = new ContactListAdapter(getActivity(), new ArrayList<Contacto>());
        OrmLiteBaseActivity<DatabaseHelper> activity = getOrmLiteBaseActivity();
        if (activity != null) {
            DatabaseHelper helper = activity.getHelper();
            RuntimeExceptionDao<Contacto, Integer> dao = helper.getContactoRuntimeDAO();
            adapter.addAll(dao.queryForAll());
        }
        // Se configura para que el adapter nofique cambios en el dataset automáticamente
        adapter.setNotifyOnChange(true);
        contactsListView.setAdapter(adapter);
    }

    private OrmLiteBaseActivity<DatabaseHelper> getOrmLiteBaseActivity() {
        Activity activity = getActivity();
        if (activity instanceof OrmLiteBaseActivity)
            return (OrmLiteBaseActivity<DatabaseHelper>) activity;
        return null;
    }

    @Override
    public void eliminarContactos() {
        String mensaje = "¿Está seguro de eliminar los contactos seleccionados?";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_action_warning).setTitle("Confirmar Operación");
        builder.setMessage(mensaje);
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SparseBooleanArray array = contactsListView.getCheckedItemPositions();
                ArrayList<Contacto> seleccion = new ArrayList<Contacto>();
                for (int j = 0; j < array.size(); j++) {
                    // Posición del contacto en el adaptador
                    int posicion = array.keyAt(j);
                    if (array.valueAt(j)) seleccion.add(adapter.getItem(posicion));
                }
                // Segundo for para eliminar los contactos del adapter y no perder los ínidces en el for anterior
                for (Contacto con : seleccion) adapter.remove(con);
                Intent intent = new Intent("listacontactos");
                intent.putExtra("operacion", ContactReceiver.CONTACTO_ELIMINADO);
                intent.putExtra("datos", seleccion);
                getActivity().sendBroadcast(intent);
                contactsListView.clearChoices();
            }
        });
        builder.setNegativeButton("NO", null);
        builder.show();
    }

    @Override
    public void sincronizarDatos() {
        Toast.makeText(getActivity(), "Sincronizar Datos", Toast.LENGTH_SHORT).show();
    }
}

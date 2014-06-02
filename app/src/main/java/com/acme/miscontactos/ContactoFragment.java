package com.acme.miscontactos;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.acme.miscontactos.entity.Contacto;

/**
 * Created by alejandro on 6/1/14.
 */
public class ContactoFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private FragmentCheckedListener listener;
    private TextView viewNombre, viewTelefono, viewEmail, viewDireccion;
    private ImageView ivContactImage;
    private CheckBox checkBox;
    private Contacto contactoActual;

    public static ContactoFragment crearInstancia(Contacto contacto, FragmentCheckedListener listener) {
        ContactoFragment cfrag = new ContactoFragment();
        cfrag.contactoActual = contacto;
        cfrag.listener = listener;
        return cfrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.listview_item, container, false);
        setHasOptionsMenu(true); // Habilitamos el ActionBar de este fragment para tener botones
        inicializarComponentes(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (contactoActual != null) {
            viewNombre.setText(contactoActual.getNombre());
            viewTelefono.setText(contactoActual.getTelefono());
            viewEmail.setText(contactoActual.getEmail());
            viewDireccion.setText(contactoActual.getDireccion());
            if (contactoActual.getImageUri() != null)
                ivContactImage.setImageURI(Uri.parse(contactoActual.getImageUri()));
            else
                ivContactImage.setImageResource(R.drawable.contacto);
        }
    }

    private void inicializarComponentes(View view) {
        viewNombre = (TextView) view.findViewById(R.id.viewNombre);
        viewTelefono = (TextView) view.findViewById(R.id.viewTelefono);
        viewEmail = (TextView) view.findViewById(R.id.viewEmail);
        viewDireccion = (TextView) view.findViewById(R.id.viewDireccion);
        ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(this);
        // Al desaparecer el ArrayAdapter por fragments, necesitamos hacer que el fragment
        // active el CheckBox si el usuario selecciona este contacto
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBox.toggle();
            }
        });
    }

    public Contacto getContactoActual() {
        return contactoActual;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (listener != null) listener.fragmentChecked(this, isChecked);
    }

    public static interface FragmentCheckedListener {
        public void fragmentChecked(ContactoFragment cfrag, boolean isChecked);
    }

}

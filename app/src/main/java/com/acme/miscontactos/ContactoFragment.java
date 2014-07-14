package com.acme.miscontactos;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.acme.miscontactos.entity.Contacto;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Created by alejandro on 6/1/14.
 */
public class ContactoFragment extends Fragment {

    @InjectView(R.id.viewNombre)
    protected TextView viewNombre;

    @InjectView(R.id.viewTelefono)
    protected TextView viewTelefono;

    @InjectView(R.id.viewEmail)
    protected TextView viewEmail;

    @InjectView(R.id.viewDireccion)
    protected TextView viewDireccion;

    @InjectView(R.id.ivContactImage)
    protected ImageView ivContactImage;

    @InjectView(R.id.checkBox)
    protected CheckBox checkBox;

    private Contacto contactoActual;
    private FragmentCheckedListener listener;

    public static ContactoFragment crearInstancia(Contacto contacto, FragmentCheckedListener listener) {
        ContactoFragment cfrag = new ContactoFragment();
        cfrag.contactoActual = contacto;
        cfrag.listener = listener;
        return cfrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.listview_item, container, false);
        ButterKnife.inject(this, rootView);
        setHasOptionsMenu(true); // Habilitamos el ActionBar de este fragment para tener botones
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // super.onViewCreated(view, savedInstanceState);
        if (contactoActual != null) {
            viewNombre.setText(contactoActual.getNombre());
            viewTelefono.setText(contactoActual.getTelefono());
            viewEmail.setText(contactoActual.getEmail());
            viewDireccion.setText(contactoActual.getDireccion());
            Picasso.with(getActivity()).load(contactoActual.getImageUri())
                    .config(Bitmap.Config.ARGB_8888).resize(800, 800).centerCrop().placeholder(R.drawable.contacto)
                    .error(R.drawable.contacto).into(ivContactImage);
        } else {
            viewNombre.setText(savedInstanceState.getString("viewNombre.text"));
            viewTelefono.setText(savedInstanceState.getString("viewTelefono.text"));
            viewEmail.setText(savedInstanceState.getString("viewEmail.text"));
            viewDireccion.setText(savedInstanceState.getString("viewDireccion.text"));
            Picasso.with(getActivity()).load(savedInstanceState.getString("ivContactImage.uri"))
                    .config(Bitmap.Config.ARGB_8888).resize(800, 800).centerCrop().placeholder(R.drawable.contacto)
                    .error(R.drawable.contacto).into(ivContactImage);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (contactoActual != null) {
            outState.putString("viewNombre.text", contactoActual.getNombre());
            outState.putString("viewTelefono.text", contactoActual.getTelefono());
            outState.putString("viewEmail.text", contactoActual.getEmail());
            outState.putString("viewDireccion.text", contactoActual.getDireccion());
            outState.putString("ivContactImage.uri", contactoActual.getImageUri());
        }
    }

    public Contacto getContactoActual() {
        return contactoActual;
    }

    @OnCheckedChanged(R.id.checkBox)
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (listener != null) listener.fragmentChecked(this, isChecked);
    }

    @OnClick(R.id.listview_item)
    public void onClick(View view) {
        checkBox.toggle();
    }

    public static interface FragmentCheckedListener {
        public void fragmentChecked(ContactoFragment cfrag, boolean isChecked);
    }

}

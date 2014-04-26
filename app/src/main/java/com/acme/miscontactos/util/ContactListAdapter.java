package com.acme.miscontactos.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.acme.miscontactos.R;

import java.util.List;

/**
 * Created by alejandro on 3/31/14.
 */
public class ContactListAdapter extends ArrayAdapter<Contacto> {

    private Activity ctx;

    public ContactListAdapter(Activity context, List<Contacto> contactos) {
        super(context, R.layout.listview_item, contactos);
        this.ctx = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if( view == null ) {
            view = ctx.getLayoutInflater().inflate(R.layout.listview_item, parent, false);
        }
        Contacto actual = this.getItem(position);
        inicializarCamposDeTexto(view, actual);
        return view;
    }

    private void inicializarCamposDeTexto(View view, Contacto actual) {
        TextView textView = (TextView) view.findViewById(R.id.viewNombre);
        textView.setText(actual.getNombre());
        textView = (TextView) view.findViewById(R.id.viewTelefono);
        textView.setText(actual.getTelefono());
        textView = (TextView) view.findViewById(R.id.viewEmail);
        textView.setText(actual.getEmail());
        textView = (TextView) view.findViewById(R.id.viewDireccion);
        textView.setText(actual.getDireccion());
        ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
        ivContactImage.setImageURI(actual.getImageUri());
    }
}

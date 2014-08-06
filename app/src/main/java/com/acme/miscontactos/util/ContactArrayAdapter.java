package com.acme.miscontactos.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acme.miscontactos.R;
import com.acme.miscontactos.entity.Contacto;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by alejandro on 8/4/14.
 */
public class ContactArrayAdapter extends ArrayAdapter<Contacto> {

    private static final String LOG_TAG = ContactArrayAdapter.class.getSimpleName();

    private Context context;
    private List<Contacto> contactos;

    public ContactArrayAdapter(Context context, int resource, List<Contacto> contactos) {
        super(context, resource, contactos);
        this.contactos = contactos;
        this.context = context;
        setNotifyOnChange(true);
    }

    private int posicionMasAlta = 4;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.listview_item, parent, false);
            holder = new ViewHolder();
            ButterKnife.inject(holder, row);
            // cada "row" tendrá su único tag, dado que el layout es compartido por cada
            // fila que aparezca en el ListView, esto optimiza recursos enormemente.
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        if (position > posicionMasAlta) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.push_up_in);
            animation.setDuration(800);
            row.startAnimation(animation);
            posicionMasAlta = position;
        } else if (position <= 4) posicionMasAlta = 4;
        inicializarContenido(row, holder, contactos.get(position));
        return row;
    }

    private void inicializarContenido(View view, ViewHolder holder, Contacto contacto) {
        holder.viewNombre.setText(contacto.getNombre());
        holder.viewTelefono.setText(contacto.getTelefono());
        holder.viewEmail.setText(contacto.getEmail());
        holder.viewDireccion.setText(contacto.getDireccion());
        Picasso.with(context).load(contacto.getImageUri())
                .config(Bitmap.Config.ARGB_8888).resize(800, 800).centerCrop().placeholder(R.drawable.contacto)
                .error(R.drawable.contacto).into(holder.ivContactImage);
    }

    //<editor-fold desc="ViewHolder Class">

    /**
     * Clase para contener una referencia al contenido del layout definido en el XML
     */
    public class ViewHolder {

        @InjectView(R.id.rootLayout)
        protected LinearLayout rootView;

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

    }
    //</editor-fold>

}

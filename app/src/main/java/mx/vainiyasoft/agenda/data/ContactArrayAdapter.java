package mx.vainiyasoft.agenda.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mx.vainiyasoft.agenda.entity.Contacto;

/**
 * Created by alejandro on 8/4/14.
 */
public class ContactArrayAdapter extends ArrayAdapter<Contacto> {

    private static final String LOG_TAG = ContactArrayAdapter.class.getSimpleName();

    private Context context;
    private int posicionMasAlta = 4;
    private List<Contacto> contactos;
    private SparseBooleanArray seleccionados = new SparseBooleanArray();

    public ContactArrayAdapter(Context context, int resource, List<Contacto> contactos) {
        super(context, resource, contactos);
        this.contactos = contactos;
        this.context = context;
        setNotifyOnChange(true);
    }

    public void setViewSelection(int position, boolean value) {
        seleccionados.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        return seleccionados.get(position);
    }

    public Set<Integer> getCurrentCheckedPositions() {
        Set<Integer> posiciones = new HashSet<Integer>(seleccionados.size());
        for(int i=0; i < seleccionados.size(); i++) posiciones.add(seleccionados.keyAt(i));
        return posiciones;
    }

    public void removeSelection(int position) {
        seleccionados.delete(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        seleccionados.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(mx.vainiyasoft.agenda.R.layout.listview_item, parent, false);
            holder = new ViewHolder();
            ButterKnife.inject(holder, row);
            // cada "row" tendrá su único tag, dado que el layout es compartido por cada
            // fila que aparezca en el ListView, esto optimiza recursos enormemente.
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        if (position > posicionMasAlta) {
            Animation animation = AnimationUtils.loadAnimation(context, mx.vainiyasoft.agenda.R.anim.push_up_in);
            animation.setDuration(800);
            row.startAnimation(animation);
            posicionMasAlta = position;
        } // else if (position <= 4) posicionMasAlta = 4;
        inicializarContenido(holder, contactos.get(position));
        return row;
    }

    private void inicializarContenido(ViewHolder holder, Contacto contacto) {
        holder.viewNombre.setText(contacto.getNombre());
        holder.viewTelefono.setText(contacto.getTelefono());
        holder.viewEmail.setText(contacto.getEmail());
        holder.viewDireccion.setText(contacto.getDireccion());
        Picasso.with(context).load(contacto.getImageUri())
                .config(Bitmap.Config.ARGB_8888).resize(800, 800).centerCrop().placeholder(mx.vainiyasoft.agenda.R.drawable.contacto)
                .error(mx.vainiyasoft.agenda.R.drawable.contacto).into(holder.ivContactImage);
    }

    //<editor-fold desc="ViewHolder Class">

    /**
     * Clase para contener una referencia al contenido del layout definido en el XML
     */
    public class ViewHolder {

        @InjectView(mx.vainiyasoft.agenda.R.id.rootLayout)
        protected LinearLayout rootView;

        @InjectView(mx.vainiyasoft.agenda.R.id.viewNombre)
        protected TextView viewNombre;

        @InjectView(mx.vainiyasoft.agenda.R.id.viewTelefono)
        protected TextView viewTelefono;

        @InjectView(mx.vainiyasoft.agenda.R.id.viewEmail)
        protected TextView viewEmail;

        @InjectView(mx.vainiyasoft.agenda.R.id.viewDireccion)
        protected TextView viewDireccion;

        @InjectView(mx.vainiyasoft.agenda.R.id.ivContactImage)
        protected ImageView ivContactImage;

    }
    //</editor-fold>

}

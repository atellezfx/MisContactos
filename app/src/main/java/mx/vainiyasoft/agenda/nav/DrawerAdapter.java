package mx.vainiyasoft.agenda.nav;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mx.vainiyasoft.agenda.R;

/**
 * Created by alejandro on 10/14/14.
 */
public class DrawerAdapter extends ArrayAdapter<String> {

    private static final String LOG_TAG = DrawerAdapter.class.getSimpleName();

    private Context context;
    private String[] titulos;

    public DrawerAdapter(Context context, int resource, String[] titulos) {
        super(context, resource, titulos);
        this.titulos = titulos;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.drawer_item, parent, false);
            holder = new ViewHolder();
            ButterKnife.inject(holder, row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        inicializarContenido(row, holder, position);
        return row;
    }

    private void inicializarContenido(View row, ViewHolder holder, int position) {
        holder.itemText.setText(titulos[position]);
        int item_image_resource = -1;
        switch (position) {
            case 0:
                item_image_resource = R.drawable.ic_menu_adduser;
                break;
            case 1:
                item_image_resource = R.drawable.ic_menu_viewlist;
                break;
            case 2:
                item_image_resource = R.drawable.ic_menu_delete;
                break;
            case 3:
                item_image_resource = R.drawable.ic_menu_sinchronize;
                break;
        }
        Picasso.with(context).load(item_image_resource).config(Bitmap.Config.ARGB_8888)
                .resize(800, 800).centerCrop().into(holder.itemIcon);
    }

    protected class ViewHolder {
        @InjectView(R.id.item_icon)
        protected ImageView itemIcon;
        @InjectView(R.id.item_text)
        protected TextView itemText;
    }
}

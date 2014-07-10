package com.acme.miscontactos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.acme.miscontactos.entity.Contacto;
import com.acme.miscontactos.util.ContactReceiver;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by alejandro on 5/2/14.
 */
public class CrearContactoFragment extends Fragment {

    @InjectView(R.id.cmpNombre)
    protected EditText txtNombre;

    @InjectView(R.id.cmpTelefono)
    protected EditText txtTelefono;

    @InjectView(R.id.cmpEmail)
    protected EditText txtEmail;

    @InjectView(R.id.cmpDireccion)
    protected EditText txtDireccion;

    @InjectView(R.id.imgContacto)
    protected ImageView imgViewContacto;

    @InjectView(R.id.btnGuardar)
    protected Button btnGuardar;

    @InjectView(R.id.btnCancelar)
    protected Button btnCancelar;

    private int request_code = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_crear_contacto, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @OnClick({R.id.btnGuardar, R.id.btnCancelar, R.id.imgContacto})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGuardar:
                guardarContacto(view);
                break;
            case R.id.btnCancelar:
                limpiarCampos();
                break;
            case R.id.imgContacto:
                cargarImagen();
                break;
        }
    }

    @OnTextChanged(R.id.cmpNombre)
    public void onTextChanged(CharSequence seq, int i, int i2, int i3) {
        btnGuardar.setEnabled(!seq.toString().trim().isEmpty());
    }

    private void cargarImagen() {
        Intent intent = null;
        // Verificamos la versión de la plataforma
        if (Build.VERSION.SDK_INT < 19) {
            // Android JellyBean 4.3 y anteriores
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            // Android KitKat 4.4 o superior
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(intent, request_code);
    }

    private void guardarContacto(View view) {
        boolean result = agregarContacto(
                txtNombre.getText().toString(),
                txtTelefono.getText().toString(),
                txtEmail.getText().toString(),
                txtDireccion.getText().toString(),
                // String.valueOf(null) regresa "null" en vez de null
                imgViewContacto.getTag() != null ? String.valueOf(imgViewContacto.getTag()) : null
                // Obtenemos el atributo TAG con la Uri de la imagen
        );
        if (result) {
            String mesg = i18n(R.string.mesg_toast_contact_added, txtNombre.getText());
            Toast.makeText(view.getContext(), mesg, Toast.LENGTH_SHORT).show();
            btnGuardar.setEnabled(false);
            limpiarCampos();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
            alert.setTitle(i18n(R.string.title_alertdialog_error));
            alert.setMessage(i18n(R.string.mesg_alertdialog_error));
            alert.setPositiveButton("OK", null);
            alert.show();
        }
    }

    private boolean agregarContacto(String nombre, String telefono, String email, String direccion, String imageUri) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String usuario = shp.getString("username", null);
        if (usuario != null) {
            Contacto nuevo = new Contacto(nombre, telefono, email, direccion, imageUri, usuario);
            Intent intent = new Intent(ContactReceiver.FILTER_NAME);
            intent.putExtra("operacion", ContactReceiver.CONTACTO_AGREGADO);
            intent.putExtra("datos", nuevo);
            getActivity().sendBroadcast(intent);
            return true;
        }
        return false;
    }

    private void limpiarCampos() {
        txtNombre.getText().clear();
        txtTelefono.getText().clear();
        txtEmail.getText().clear();
        txtDireccion.getText().clear();
        // Restablecemos la imagen predeterminada del contacto
        imgViewContacto.setImageResource(R.drawable.contacto);
        txtNombre.requestFocus();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == request_code) {
            Uri uri = data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int takeFlags = data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                ContentResolver resolver = getActivity().getContentResolver();
                resolver.takePersistableUriPermission(uri, takeFlags);
            }
            Picasso.with(getActivity()).load(uri).config(Bitmap.Config.ARGB_8888).resize(800, 800).centerCrop()
                    .placeholder(R.drawable.contacto).error(R.drawable.contacto).into(imgViewContacto);
            // Utilizamos el atributo TAG para almacenar la Uri al archivo seleccionado
            imgViewContacto.setTag(uri);
        }
    }

    private String i18n(int resourceId, Object... formatArgs) {
        return getResources().getString(resourceId, formatArgs);
    }

}

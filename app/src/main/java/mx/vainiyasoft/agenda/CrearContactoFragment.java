package mx.vainiyasoft.agenda;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import mx.vainiyasoft.agenda.data.ContactOperations;
import mx.vainiyasoft.agenda.entity.Contacto;
import mx.vainiyasoft.agenda.util.PhotoCopier;
import mx.vainiyasoft.agenda.util.SelectPictureBridge;

/**
 * Created by alejandro on 5/2/14.
 */
public class CrearContactoFragment extends BaseFragment {

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
    protected ImageButton btnGuardar;

    private SelectPictureBridge bridge = new SelectPictureBridge(this);

    private static final String LOG_TAG = CrearContactoFragment.class.getSimpleName();
    public static final int LOAD_IMAGE_REQUEST_CODE = 0;
    public static final int IMPORT_QR_REQUEST_CODE = 1;
    public static final int TAKE_PICTURE_REQUEST_CODE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_crear_contacto, container, false);
        ButterKnife.inject(this, rootView);
        btnGuardar.setEnabled(false);
        return rootView;
    }

    @OnClick({R.id.btnGuardar, R.id.btnCancelar, R.id.btnImportar})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGuardar:
                guardarContacto(view);
                break;
            case R.id.btnCancelar:
                limpiarCampos();
                break;
            case R.id.btnImportar:
                importarCodigoQR();
                break;
        }
    }

    @OnClick(R.id.imgContacto)
    public void onImageClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_picture_title);
        builder.setItems(R.array.select_picture_options, bridge.getDialogOnClickListener());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnTextChanged(R.id.cmpNombre)
    public void onTextChanged(CharSequence seq, int i, int i2, int i3) {
        btnGuardar.setEnabled(!seq.toString().trim().isEmpty());
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
            Intent intent = new Intent(ContactOperations.FILTER_NAME);
            intent.putExtra("operacion", ContactOperations.ACCION_AGREGAR_CONTACTO);
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

    private void importarCodigoQR() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, IMPORT_QR_REQUEST_CODE);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case LOAD_IMAGE_REQUEST_CODE:
                    PhotoCopier copier = new PhotoCopier(getActivity());
                    Uri imgFileUri = copier.copyImageUri(data.getData());
                    Picasso.with(getActivity()).load(imgFileUri).config(Bitmap.Config.ARGB_8888).resize(800, 800)
                            .centerCrop().placeholder(R.drawable.contacto).error(R.drawable.contacto).into(imgViewContacto);
                    // Utilizamos el atributo TAG para almacenar la Uri al archivo seleccionado
                    imgViewContacto.setTag(imgFileUri);
                    break;
                case IMPORT_QR_REQUEST_CODE:
                    try {
                        String result = data.getStringExtra("SCAN_RESULT");
                        Contacto bean = MainActivity.getObjectMapper().readValue(result, Contacto.class);
                        txtNombre.setText(bean.getNombre());
                        txtTelefono.setText(bean.getTelefono());
                        txtEmail.setText(bean.getEmail());
                        txtDireccion.setText(bean.getDireccion());
                        txtNombre.requestFocus();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                    }
                    break;
                case TAKE_PICTURE_REQUEST_CODE:
                    // TODO: Eliminar este Log después de la fase de pruebas
                    Log.d(LOG_TAG, "URI DE IMAGEN: " + String.valueOf(bridge.getPictureFileUri()));
                    Picasso.with(getActivity()).load(bridge.getPictureFileUri()).config(Bitmap.Config.ARGB_8888)
                            .resize(800, 800).centerCrop().placeholder(R.drawable.contacto).error(R.drawable.contacto)
                            .into(imgViewContacto);
                    // Utilizamos el atributo TAG para almacenar la Uri al archivo seleccionado
                    imgViewContacto.setTag(bridge.getPictureFileUri());
                    break;
            }
        }
    }

    private String i18n(int resourceId, Object... formatArgs) {
        return getResources().getString(resourceId, formatArgs);
    }

}

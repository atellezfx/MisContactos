package mx.vainiyasoft.agenda.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mx.vainiyasoft.agenda.MainActivity;
import mx.vainiyasoft.agenda.entity.Contacto;

import static android.content.DialogInterface.OnClickListener;

/**
 * Created by alejandro on 8/13/14.
 */
public class ShareOptionsBridge {

    private final String LOG_TAG = ShareOptionsBridge.class.getSimpleName();
    private final int OPTION_QRCODE = 0, OPTION_BLUETOOTH = 1, OPTION_OTHER = 2;
    private final ContactArrayAdapter listAdapter;
    private ObjectMapper mapper;
    private Activity activity;

    public ShareOptionsBridge(ContactArrayAdapter listAdapter, Activity activity) {
        mapper = MainActivity.getObjectMapper();
        this.listAdapter = listAdapter;
        this.activity = activity;
    }

    public OnClickListener getDialogOnClickListener(final int position) {
        return new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int option) {
                Contacto bean = listAdapter.getItem(position);
                switch (option) {
                    case OPTION_QRCODE:
                        shareViewQRCode(bean);
                        break;
                    case OPTION_BLUETOOTH:
                        shareViaBluetooth(bean);
                        break;
                    case OPTION_OTHER:
                        shareViaOther(bean);
                        break;
                }
            }
        };
    }

    private void shareViewQRCode(Contacto bean) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
            intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
            intent.putExtra("ENCODE_FORMAT", "QR_CODE");
            String encodeData = mapper.writeValueAsString(bean);
            intent.putExtra("ENCODE_DATA", encodeData);
            activity.startActivity(intent);
        } catch (JsonProcessingException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
    }

    private void shareViaBluetooth(Contacto bean) {
        Context context = ApplicationContextProvider.getContext();
        Toast.makeText(context, "Compartiendo via bluetooth", Toast.LENGTH_SHORT).show();
        // TODO: Implmentar compartir por bluetooth
    }

    private void shareViaOther(Contacto bean) {
        try {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/json");
            String shareBody = mapper.writeValueAsString(bean);
            String subject = String.format("Contacto: %s", bean.getNombre());
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            activity.startActivity(Intent.createChooser(sharingIntent, "Compartir por:"));
        } catch (JsonProcessingException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
    }


}

package mx.vainiyasoft.agenda.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by alejandro on 8/27/14.
 */
public class WifiMonitor extends BroadcastReceiver {

    private static final String LOG_TAG = WifiMonitor.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // El objeto intent contiene los detalles sobre el estado de la conexión por wifi
        // esto facilita la operación de detectar si el wifi está activado o no
        // y podría evitar tener que solicitar el permiso ACCESS_WIFI_STATE y ACCESS_NETWORK_STATE
        int estado = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
        Log.d(LOG_TAG, String.format("Estado WiFi: %d", estado));
        switch (estado) {
            case WifiManager.WIFI_STATE_DISABLED:
                Log.d(LOG_TAG, "WIFI DESACTIVADO");
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                Log.d(LOG_TAG, "WIFI DESACTIVANDOSE");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                // TODO: Aquí podroemos activar el proceso de sincronización al servidor
                Log.d(LOG_TAG, "WIFI ACTIVADO");
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                Log.d(LOG_TAG, "WIFI ACTIVANDOSE");
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                Log.d(LOG_TAG, "WIFI CON ESTADO DESCONOCIDO");
                break;
        }
    }
}

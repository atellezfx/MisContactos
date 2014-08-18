package mx.vainiyasoft.agenda.net;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import mx.vainiyasoft.agenda.entity.JSONBean;
import mx.vainiyasoft.agenda.util.ApplicationContextProvider;

/**
 * Created by alejandro on 6/16/14.
 */
public class HttpServiceBroker extends BroadcastReceiver {

    public static final String FILTER_NAME = "service_broker";
    public static final int SYNC_SERVICE_NOTIFICATION_ID = 12345;
    public static final int HTTP_GET_METHOD = 1, HTTP_POST_METHOD = 2, HTTP_PUT_METHOD = 3, HTTP_DELETE_METHOD = 4;
    private String base_url_address, propietario;

    @Override
    public void onReceive(Context context, Intent intent) {
        loadPreferences(context);
        switch (intent.getIntExtra("metodo_http", 0)) {
            case HTTP_GET_METHOD:
                intent.putExtra("url", String.format("%s/owner/%s", base_url_address, propietario));
                performRequest(context, intent, HttpGetService.class);
                break;
            case HTTP_POST_METHOD:
                ArrayList<JSONBean> createList = intent.getParcelableArrayListExtra("datos");
                Log.i("HTTP_POST_METHOD", createList.toString());
                for (int i = 0; i < createList.size(); i++) {
                    intent.putExtra("url", base_url_address);
                    intent.putExtra("bean", createList.get(i));
                    intent.putExtra("maxProgress", createList.size());
                    intent.putExtra("currentProgress", i + 1);
                    performRequest(context, intent, HttpPostService.class);
                }
                break;
            case HTTP_PUT_METHOD:
                ArrayList<JSONBean> updateList = intent.getParcelableArrayListExtra("datos");
                Log.i("HTTP_PUT_METHOD", updateList.toString());
                for (int i = 0; i < updateList.size(); i++) {
                    JSONBean bean = updateList.get(i);
                    intent.putExtra("url", String.format("%s/%d", base_url_address, bean.getServerId()));
                    intent.putExtra("bean", bean);
                    intent.putExtra("maxProgress", updateList.size());
                    intent.putExtra("currentProgress", i + 1);
                    performRequest(context, intent, HttpPutService.class);
                }
                break;
            case HTTP_DELETE_METHOD:
                ArrayList<JSONBean> deleteList = intent.getParcelableArrayListExtra("datos");
                Log.i("HTTP_DELETE_METHOD", deleteList.toString());
                for (int i = 0; i < deleteList.size(); i++) {
                    JSONBean bean = deleteList.get(i);
                    intent.putExtra("url", String.format("%s/%d", base_url_address, bean.getServerId()));
                    intent.putExtra("bean", bean);
                    intent.putExtra("maxProgress", deleteList.size());
                    intent.putExtra("currentProgress", i + 1);
                    performRequest(context, intent, HttpDeleteService.class);
                }
                break;
        }
    }

    private void performRequest(Context context, Intent intent, Class<? extends IntentService> serviceClass) {
        if (wifiEnabled(context)) {
            Intent requestIntent = new Intent(context, serviceClass);
            requestIntent.putExtras(intent);
            if (requestIntent.hasExtra("datos")) requestIntent.removeExtra("datos");
            context.startService(requestIntent);
        } else {
            Toast.makeText(context, i18n(mx.vainiyasoft.agenda.R.string.mesg_wifi_not_available),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean wifiEnabled(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

    private void loadPreferences(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        String address = shp.getString("server_address", null);
        String port = shp.getString("server_port", null);
        base_url_address = String.format("http://%s:%s/jsonweb/rest/contacto", address, port);
        propietario = shp.getString("username", null);
    }

    private String i18n(int resourceId, Object... formatArgs) {
        Context ctx = ApplicationContextProvider.getContext();
        return ctx.getResources().getString(resourceId, formatArgs);
    }

}

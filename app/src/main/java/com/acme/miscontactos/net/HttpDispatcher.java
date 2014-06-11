package com.acme.miscontactos.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.acme.miscontactos.entity.JSONBean;
import com.acme.miscontactos.util.AsyncTaskListener;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Created by alejandro on 6/10/14.
 */
public class HttpDispatcher {

    private final Context context;
    private ObjectMapper mapper;
    // URL del proyecto Desarrollo en Tu Idioma - Servicios Rest con NetBeans
    private final String BASE_URL_ADDRESS = "http://%s:%s/jsonweb/rest/contacto";
    private final String SERVER_ADDRESS;
    private final String SERVER_PORT;
    private final String REGISTRY_OWNER;

    public HttpDispatcher(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SERVER_ADDRESS = prefs.getString("server_address", null);
        SERVER_PORT = prefs.getString("server_port", null);
        REGISTRY_OWNER = prefs.getString("username", null);
        mapper = new ObjectMapper();
        // Desactivar la autodetección y obligar al uso de atributos y no de getter/setter
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        this.context = context;
    }

    public <T> void doGet(Class<T> resultType, AsyncTaskListener<T> listener) {
        StringBuilder builder = new StringBuilder(String.format(BASE_URL_ADDRESS, SERVER_ADDRESS, SERVER_PORT));
        String url = builder.append("/owner/").append(REGISTRY_OWNER).toString();
        if (wifiEnabled()) {
            HttpGetWorker<T> worker = new HttpGetWorker<T>(mapper, resultType);
            worker.addAsyncTaskListener(listener);
            worker.execute(url);
        } else {
            Toast.makeText(context, "WiFi no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    public void doPost(JSONBean bean, AsyncTaskListener<List<String>> listener) {
        StringBuilder builder = new StringBuilder(String.format(BASE_URL_ADDRESS, SERVER_ADDRESS, SERVER_PORT));
        String url = builder.toString();
        if (wifiEnabled()) {
            HttpPostWorker worker = new HttpPostWorker(mapper, url);
            worker.addAsyncTaskListener(listener);
            worker.execute(bean);
        } else {
            Toast.makeText(context, "WiFi no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    public void doPut(JSONBean bean, AsyncTaskListener<List<String>> listener) {
        StringBuilder builder = new StringBuilder(String.format(BASE_URL_ADDRESS, SERVER_ADDRESS, SERVER_PORT));
        String url = builder.append("/").append(bean.getServerId()).toString();
        if (wifiEnabled()) {
            HttpPutWorker worker = new HttpPutWorker(mapper, url);
            worker.addAsyncTaskListener(listener);
            worker.execute(bean);
        } else {
            Toast.makeText(context, "WiFi no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDelete(JSONBean bean, AsyncTaskListener<List<String>> listener) {
        StringBuilder builder = new StringBuilder(String.format(BASE_URL_ADDRESS, SERVER_ADDRESS, SERVER_PORT));
        String url = builder.append("/").append(bean.getServerId()).toString();
        if (wifiEnabled()) {
            HttpDeleteWorker worker = new HttpDeleteWorker(mapper);
            worker.addAsyncTaskListener(listener);
            worker.execute(url);
        } else {
            Toast.makeText(context, "WiFi no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean wifiEnabled() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

}

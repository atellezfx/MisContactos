package com.acme.miscontactos.net;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.acme.miscontactos.util.AsyncTaskListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashSet;

/**
 * Created by alejandro on 6/10/14.
 */
public class HttpGetWorker<T> extends AsyncTask<String, Void, T> {

    private final ProgressDialog dialogo;
    private HashSet<AsyncTaskListener<T>> listeners;
    private final ObjectMapper mapper;
    private Class<T> beanClass;

    public HttpGetWorker(ObjectMapper mapper, Class<T> beanClass, Context context) {
        dialogo = new ProgressDialog(context);
        this.mapper = mapper;
        this.beanClass = beanClass;
    }

    @Override
    protected T doInBackground(String... strings) {
        return process(strings[0]);
    }

    @Override
    protected void onPreExecute() {
        dialogo.setTitle("Tarea Descarga");
        dialogo.setMessage("Descargando datos del servidor...");
        dialogo.show();
    }

    @Override
    protected void onPostExecute(T result) {
        for (AsyncTaskListener<T> listener : listeners) {
            listener.processResult(result);
        }
        if (dialogo.isShowing()) dialogo.dismiss();
    }

    public void addAsyncTaskListener(AsyncTaskListener<T> listener) {
        if (listeners == null) listeners = new HashSet<AsyncTaskListener<T>>();
        listeners.add(listener);
    }

    private T process(String url) {
        T data = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-Type", "application/json");
        try {
            HttpResponse resp = client.execute(httpGet);
            StatusLine statusLine = resp.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = resp.getEntity();
                String respStr = EntityUtils.toString(entity);
                data = mapper.readValue(respStr, beanClass);
            } else {
                // TODO: Mostrar alerta al usuario, notificando del error
                Log.e("JSON", "Error al cargar el documento json");
            }
        } catch (IOException ex) {
            Log.e("HttpGetWorker", ex.getLocalizedMessage(), ex);
        }
        return data;
    }
}

package com.acme.miscontactos.net;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.acme.miscontactos.entity.JSONBean;
import com.acme.miscontactos.util.AsyncTaskListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alejandro on 6/10/14.
 */
public class HttpPutWorker extends AsyncTask<JSONBean, Void, List<String>> {

    private final ProgressDialog dialogo;
    private HashSet<AsyncTaskListener<List<String>>> listeners;
    private final ObjectMapper mapper;
    private final String url;

    public HttpPutWorker(ObjectMapper mapper, String url, Context context) {
        dialogo = new ProgressDialog(context); // Debe utilizar un contexto de un Activity, no de aplicación
        this.mapper = mapper;
        this.url = url;
    }

    @Override
    protected List<String> doInBackground(JSONBean... params) {
        ArrayList<String> list = new ArrayList<String>();
        for (JSONBean bean : params) list.add(process(bean));
        return list;
    }

    @Override
    protected void onPreExecute() {
        dialogo.setTitle("Tarea Actualización");
        dialogo.setMessage("Actualizando datos del servidor...");
        dialogo.show();
    }

    @Override
    protected void onPostExecute(List<String> result) {
        for (AsyncTaskListener<List<String>> listener : listeners) {
            listener.processResult(result);
        }
        if (dialogo.isShowing()) dialogo.dismiss();
    }

    public void addAsyncTaskListener(AsyncTaskListener<List<String>> listener) {
        if (listeners == null) listeners = new HashSet<AsyncTaskListener<List<String>>>();
        listeners.add(listener);
    }

    public String process(JSONBean bean) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json");
        try {
            HttpResponse res = client.execute(httpPut);
            StatusLine statusLine = res.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = res.getEntity();
                String respStr = EntityUtils.toString(entity);
                builder.append(respStr);
            } else {
                Log.e("JSON", "Error al leer la respuesta");
            }
        } catch (IOException ex) {
            Log.e("HttpPutWorker", ex.getLocalizedMessage(), ex);
        }
        return builder.toString();
    }

}

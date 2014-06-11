package com.acme.miscontactos.net;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.acme.miscontactos.util.AsyncTaskListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alejandro on 6/10/14.
 */
public class HttpDeleteWorker extends AsyncTask<String, Void, List<String>> {

    private final ProgressDialog dialogo;
    private HashSet<AsyncTaskListener<List<String>>> listeners;
    private final ObjectMapper mapper;

    public HttpDeleteWorker(ObjectMapper mapper, Context context) {
        dialogo = new ProgressDialog(context);
        this.mapper = mapper;
    }

    @Override
    protected List<String> doInBackground(String... params) {
        ArrayList<String> list = new ArrayList<String>();
        for (String url : params) list.add(process(url));
        return list;
    }

    @Override
    protected void onPreExecute() {
        dialogo.setTitle("Tarea Eliminación");
        dialogo.setMessage("Sincronizando datos...");
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

    public String process(String url) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.addHeader("Content-Type", "application/json");
        try {
            HttpResponse resp = client.execute(httpDelete);
            String respStr = EntityUtils.toString(resp.getEntity());
            // TODO: Eliminar Log e implementar una notificación al usuario
            Log.i("DELETE RESPONSE JSON STRING", respStr);
        } catch (IOException ex) {
            Log.e("HttpDeleteWorker", ex.getLocalizedMessage(), ex);
        }
        return builder.toString();
    }

}

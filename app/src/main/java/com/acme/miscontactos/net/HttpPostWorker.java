package com.acme.miscontactos.net;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.acme.miscontactos.entity.JSONBean;
import com.acme.miscontactos.util.ApplicationContextProvider;
import com.acme.miscontactos.util.AsyncTaskListener;
import com.acme.miscontactos.util.ContactReceiver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alejandro on 6/10/14.
 */
public class HttpPostWorker extends AsyncTask<JSONBean, Void, List<String>> {

    private HashSet<AsyncTaskListener<List<String>>> listeners;
    private final ObjectMapper mapper;
    private final String url;

    public HttpPostWorker(ObjectMapper mapper, String url) {
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
    protected void onPostExecute(List<String> result) {
        for (AsyncTaskListener<List<String>> listener : listeners) {
            listener.processResult(result);
        }
    }

    public void addAsyncTaskListener(AsyncTaskListener<List<String>> listener) {
        if (listeners == null) listeners = new HashSet<AsyncTaskListener<List<String>>>();
        listeners.add(listener);
    }

    public String process(JSONBean bean) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        try {
            String data = mapper.writeValueAsString(bean);
            StringEntity entity = new StringEntity(data);
            httpPost.setEntity(entity);
            HttpResponse res = client.execute(httpPost);
            String respStr = EntityUtils.toString(res.getEntity());
            updateServerId(respStr, bean);
        } catch (IOException ex) {
            Log.e("HttpPostWorker", ex.getLocalizedMessage(), ex);
        }
        return builder.toString();
    }

    private void updateServerId(String respStr, JSONBean bean) {
        try {
            JsonNode node = mapper.readTree(respStr);
            int serverId = node.path("serverId").asInt();
            // TODO: Eliminar Log al finalizar pruebas
            Log.i("ServerID Recibido", String.valueOf(serverId));
            bean.setServerId(serverId);
            Intent intent = new Intent(ContactReceiver.FILTER_NAME);
            intent.putExtra("datos", bean);
            Context ctx = ApplicationContextProvider.getContext();
            ctx.sendBroadcast(intent);
        } catch (IOException e) {
            Log.e("HttpPostWorker", e.getLocalizedMessage(), e);
        }
    }

}

package com.acme.miscontactos.net;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.acme.miscontactos.MainActivity;
import com.acme.miscontactos.entity.JSONBean;
import com.acme.miscontactos.util.ApplicationContextProvider;
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

/**
 * Created by alejandro on 6/16/14.
 */
public class HttpPostService extends IntentService {

    private final ObjectMapper mapper;

    public HttpPostService() {
        super("HttpPostService");
        mapper = MainActivity.getObjectMapper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(intent.getStringExtra("url"));
        httpPost.addHeader("Content-Type", "application/json");
        try {
            JSONBean bean = intent.getParcelableExtra("bean");
            String data = mapper.writeValueAsString(bean);
            StringEntity entity = new StringEntity(data);
            httpPost.setEntity(entity);
            HttpResponse resp = client.execute(httpPost);
            String respStr = EntityUtils.toString(resp.getEntity());
            processResponse(respStr, bean);
        } catch (IOException ex) {
            Log.e("HttpPostService", ex.getLocalizedMessage(), ex);
        }
    }

    private void processResponse(String respStr, JSONBean bean) {
        try {
            JsonNode node = mapper.readTree(respStr);
            int serverId = node.path("serverId").asInt();
            // TODO: Eliminar Log al finalizar las pruebas
            Log.i("ServerID Recibido", String.valueOf(serverId));
            bean.setServerId(serverId);
            Intent intent = new Intent(ContactReceiver.FILTER_NAME);
            intent.putExtra("operacion", ContactReceiver.CONTACTO_ACTUALIZADO);
            intent.putExtra("datos", bean);
            Context ctx = ApplicationContextProvider.getContext();
            ctx.sendBroadcast(intent);
        } catch (IOException ex) {
            Log.e("HttpPostService", ex.getLocalizedMessage(), ex);
        }
    }
}

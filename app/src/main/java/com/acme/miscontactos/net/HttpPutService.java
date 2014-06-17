package com.acme.miscontactos.net;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.acme.miscontactos.MainActivity;
import com.acme.miscontactos.entity.JSONBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alejandro on 6/16/14.
 */
public class HttpPutService extends IntentService {

    private final ObjectMapper mapper;

    public HttpPutService() {
        super("HttpPutService");
        mapper = MainActivity.getObjectMapper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HttpClient client = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(intent.getStringExtra("url"));
        httpPut.addHeader("Content-Type", "application/json");
        try {
            JSONBean bean = intent.getParcelableExtra("bean");
            String data = mapper.writeValueAsString(bean);
            StringEntity stringEntity = new StringEntity(data);
            httpPut.setEntity(stringEntity);
            HttpResponse resp = client.execute(httpPut);
            StatusLine statusLine = resp.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = resp.getEntity();
                String respStr = EntityUtils.toString(entity);
                // TODO: Eliminar Log e implementar una notificación al usuario
                Log.i("HTTP PUT RESPONSE JSON STRING", respStr);
                processResponse(respStr);
            } else {
                Log.e("JSON", "Error al leer la respuesta");
            }
        } catch (IOException ex) {
            Log.e("HttpPutService", ex.getLocalizedMessage(), ex);
        }
    }

    private void processResponse(String respStr) throws IOException {
        HashMap<String, String> data = mapper.readValue(respStr, HashMap.class);
        // TODO: Implementar código del mapa (alguna notificación al usuario)
    }
}

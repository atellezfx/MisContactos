package com.acme.miscontactos.net;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.acme.miscontactos.MainActivity;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by alejandro on 6/16/14.
 */
public class HttpGetService extends IntentService {

    private final ObjectMapper mapper;

    public HttpGetService() {
        super("HttpGetService");
        mapper = MainActivity.getObjectMapper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String entityClassName = intent.getStringExtra("entity_class_name");
            Class<?> beanClass = (Class<?>) Class.forName(entityClassName);
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(intent.getStringExtra("url"));
            httpGet.addHeader("Content-Type", "application/json");
            HttpResponse resp = client.execute(httpGet);
            StatusLine statusLine = resp.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = resp.getEntity();
                String respStr = EntityUtils.toString(entity);
                processResponse(respStr, beanClass);
            } else {
                // TODO: Mostrar alerta al usuario, notificando del error
                Log.e("HTTP GET JSON", "Error al cargar el documento json");
            }
        } catch (Exception ex) {
            Log.e("HttpGetService", ex.getLocalizedMessage(), ex);
        }
    }

    private <T> void processResponse(String respStr, Class<T> beanClass) throws IOException {
        T data = mapper.readValue(respStr, beanClass);
        if (data.getClass().isArray()) {
            T[] array = (T[]) data;
            // TODO: Implementar código para procesar arreglo
        } else {
            // TODO: Implementar código para procesar objeto
        }
    }
}

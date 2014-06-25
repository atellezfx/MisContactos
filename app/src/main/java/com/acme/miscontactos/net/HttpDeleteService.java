package com.acme.miscontactos.net;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.acme.miscontactos.MainActivity;
import com.acme.miscontactos.util.NotificationController;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by alejandro on 6/16/14.
 */
public class HttpDeleteService extends IntentService {

    public final int NOTIFICATION_ID = HttpServiceBroker.SYNC_SERVICE_NOTIFICATION_ID + HttpServiceBroker.HTTP_DELETE_METHOD;
    private final ObjectMapper mapper;

    public HttpDeleteService() {
        super("HttpDeleteService");
        mapper = MainActivity.getObjectMapper();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HttpClient client = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(intent.getStringExtra("url"));
        httpDelete.addHeader("Content-Type", "application/json");
        try {
            HttpResponse resp = client.execute(httpDelete);
            StatusLine statusLine = resp.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = resp.getEntity();
                String respStr = EntityUtils.toString(entity);
                processResponse(intent, respStr);
            } else {
                Log.e("JSON", "Error al leer la respuesta");
            }
        } catch (IOException ex) {
            Log.e("HttpDeleteService", ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void onDestroy() {
        NotificationController.notify("Agenda", "Sincronizando datos eliminados...", NOTIFICATION_ID);
        super.onDestroy();
    }

    private void processResponse(Intent intent, String respStr) throws IOException {
        HashMap<String, String> data = mapper.readValue(respStr, HashMap.class);
        // TODO: Eliminar Log.i después de la fase de pruebas
        Log.i("HTTP_DELETE RESPONSE: ", String.valueOf(data));
        notificarRespuesta(intent);
    }

    private void notificarRespuesta(Intent intent) {
        int maxProgress = intent.getIntExtra("maxProgress", -1);
        int currentProgress = intent.getIntExtra("currentProgress", -1);
        NotificationController.notify("Agenda", "Sincronizando datos eliminados...",
                NOTIFICATION_ID, currentProgress, maxProgress);
    }

}

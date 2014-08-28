package mx.vainiyasoft.agenda.net;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

import mx.vainiyasoft.agenda.data.DataChangeTracker;
import mx.vainiyasoft.agenda.entity.JSONBean;

/**
 * Created by alejandro on 8/27/14.
 */
public class NetworkBridge {

    private final Activity activity;

    public NetworkBridge(Activity activity) {
        this.activity = activity;
    }

    public void sincronizarDatos() {
        DataChangeTracker tracker = new DataChangeTracker(activity);
        ArrayList<DataChangeTracker.StoredRecord> allRecords = tracker.retrieveRecords();
        ArrayList<DataChangeTracker.StoredRecord> createList = new ArrayList<DataChangeTracker.StoredRecord>();
        ArrayList<DataChangeTracker.StoredRecord> deleteList = new ArrayList<DataChangeTracker.StoredRecord>();
        ArrayList<DataChangeTracker.StoredRecord> updateList = new ArrayList<DataChangeTracker.StoredRecord>();
        for (DataChangeTracker.StoredRecord record : allRecords) {
            switch (record.getType()) {
                case DataChangeTracker.StoredRecord.TYPE_CREATE:
                    createList.add(record);
                    break;
                case DataChangeTracker.StoredRecord.TYPE_DELETE:
                    deleteList.add(record);
                    break;
                case DataChangeTracker.StoredRecord.TYPE_UPDATE:
                    updateList.add(record);
                    break;
            }
        }
        doPost(createList);
        doPut(updateList);
        doDelete(deleteList);
        tracker.clearRecords();
    }

    private void doDelete(ArrayList<DataChangeTracker.StoredRecord> deleteList) {
        Intent intent = new Intent(HttpServiceBroker.FILTER_NAME);
        intent.putExtra("metodo_http", HttpServiceBroker.HTTP_DELETE_METHOD);
        ArrayList<JSONBean> datos = new ArrayList<JSONBean>();
        for (DataChangeTracker.StoredRecord record : deleteList) datos.add(record.getData());
        intent.putParcelableArrayListExtra("datos", datos);
        activity.sendBroadcast(intent);
    }

    private void doPut(ArrayList<DataChangeTracker.StoredRecord> updateList) {
        Intent intent = new Intent(HttpServiceBroker.FILTER_NAME);
        intent.putExtra("metodo_http", HttpServiceBroker.HTTP_PUT_METHOD);
        ArrayList<JSONBean> datos = new ArrayList<JSONBean>();
        for (DataChangeTracker.StoredRecord record : updateList) datos.add(record.getData());
        intent.putParcelableArrayListExtra("datos", datos);
        activity.sendBroadcast(intent);
    }

    private void doPost(ArrayList<DataChangeTracker.StoredRecord> createList) {
        Intent intent = new Intent(HttpServiceBroker.FILTER_NAME);
        intent.putExtra("metodo_http", HttpServiceBroker.HTTP_POST_METHOD);
        ArrayList<JSONBean> datos = new ArrayList<JSONBean>();
        for (DataChangeTracker.StoredRecord record : createList) datos.add(record.getData());
        intent.putParcelableArrayListExtra("datos", datos);
        activity.sendBroadcast(intent);
    }

}

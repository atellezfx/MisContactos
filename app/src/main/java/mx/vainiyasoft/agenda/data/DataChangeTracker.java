package mx.vainiyasoft.agenda.data;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import mx.vainiyasoft.agenda.MainActivity;
import mx.vainiyasoft.agenda.entity.JSONBean;

/**
 * Created by alejandro on 6/10/14.
 */
public class DataChangeTracker {

    private final static String LOG_TAG = DataChangeTracker.class.getSimpleName();

    private final String SYNC_FILE_NAME = "agenda_sync.txt";
    private final String IMGURIS_FILE_NAME = "revisar_uris.txt";

    private final ObjectMapper mapper;
    private final Context ctx;
    private final int OPERATION_TYPE = 0, STORED_CLASS_NAME = 1, JSON_BEAN = 2;

    public DataChangeTracker(Context ctx) {
        mapper = MainActivity.getObjectMapper();
        this.ctx = ctx;
    }

    public void recordCreateOp(JSONBean bean) {
        storeRecord("C:%s:%s", bean);
    }

    public void recordUpdateOp(JSONBean bean) {
        storeRecord("U:%s:%s", bean);
    }

    public void recordDeleteOp(JSONBean bean) {
        storeRecord("D:%s:%s", bean);
    }

    public void recordImgUri(String imgUri) {
        storeRecord(imgUri, IMGURIS_FILE_NAME);
    }

    //<editor-fold desc="STORE RECORD METHODS">
    private void storeRecord(String operacion, JSONBean bean) {
        try {
            String className = bean.getClass().getName();
            String record = String.format(operacion, className, mapper.writeValueAsString(bean));
            storeRecord(record, SYNC_FILE_NAME);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
    }

    private void storeRecord(String record, String filename) {
        try {
            FileOutputStream fos = ctx.openFileOutput(filename, Context.MODE_APPEND);
            PrintStream out = new PrintStream(fos);
            out.println(record); //Autoflush
            out.close();
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="RETRIEVE RECORD METHODS AND CLASS">
    public ArrayList<StoredRecord> retrieveStoredRecords() {
        ArrayList<StoredRecord> recordList = new ArrayList<StoredRecord>();
        try {
            FileInputStream fis = ctx.openFileInput(SYNC_FILE_NAME);
            Scanner scn = new Scanner(fis);
            while (scn.hasNextLine()) {
                String line = scn.nextLine();
                StoredRecord record = readStoredRecord(line);
                if (record != null) recordList.add(record);
            }
        } catch (Exception e) {
            Log.e("DataChangeTracker.retrieveStoredRecords()", e.getLocalizedMessage(), e);
        }
        return recordList;
    }

    private StoredRecord readStoredRecord(String line) throws ClassNotFoundException, IOException {
        StoredRecord record = null;
        String[] array = line.split(":", 3);
        char type = array[OPERATION_TYPE].charAt(0);
        Class storedClass = Class.forName(array[STORED_CLASS_NAME]);
        JSONBean bean = (JSONBean) mapper.readValue(array[JSON_BEAN], storedClass);
        record = new StoredRecord(type, bean);
        return record;
    }

    public void clearStoredRecords() {
        clearRecords(SYNC_FILE_NAME);
    }


    public static class StoredRecord {
        public static final char TYPE_CREATE = 'C', TYPE_DELETE = 'D', TYPE_UPDATE = 'U';
        private char type;
        private JSONBean data;

        public StoredRecord(char type, JSONBean data) {
            this.type = type;
            this.data = data;
        }

        public char getType() {
            return type;
        }

        public JSONBean getData() {
            return data;
        }
    }
    //</editor-fold>

    public ArrayList<String> retrieveImgUriRecords() {
        ArrayList<String> recordList = new ArrayList<String>();
        try {
            File file = ctx.getFileStreamPath(IMGURIS_FILE_NAME);
            if (file.exists()) {
                FileInputStream fis = ctx.openFileInput(IMGURIS_FILE_NAME);
                Scanner scn = new Scanner(fis);
                while (scn.hasNextLine()) {
                    String line = scn.nextLine();
                    recordList.add(line);
                }
                scn.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
        return recordList;
    }

    public void clearImgUriRecords() {
        clearRecords(IMGURIS_FILE_NAME);
    }

    private void clearRecords(String filename) {
        try {
            FileOutputStream fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            PrintStream out = new PrintStream(fos);
            out.close();
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
    }

}

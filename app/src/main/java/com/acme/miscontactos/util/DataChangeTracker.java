package com.acme.miscontactos.util;

import android.content.Context;
import android.util.Log;

import com.acme.miscontactos.entity.JSONBean;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by alejandro on 6/10/14.
 */
public class DataChangeTracker {

    private final String FILE_NAME = "agenda_sync.txt";
    private final ObjectMapper mapper;
    private final Context ctx;
    private final int OPERATION_TYPE = 0, STORED_CLASS_NAME = 1, JSON_BEAN = 2;

    public DataChangeTracker(Context ctx) {
        mapper = new ObjectMapper();
        // Desactivar la autodetección y obligar al uso de atributos y no de getter/setter
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
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

    private void storeRecord(String operacion, JSONBean bean) {
        try {
            FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_APPEND);
            String className = bean.getClass().getName();
            String record = String.format(operacion, className, mapper.writeValueAsString(bean));
            PrintStream out = new PrintStream(fos);
            out.println(record); // Autoflush
            out.close();
        } catch (Exception e) {
            Log.e("DataChangeTracker.storeRecord()", e.getLocalizedMessage(), e);
        }
    }

    public ArrayList<StoredRecord> retrieveRecords() {
        ArrayList<StoredRecord> recordList = new ArrayList<StoredRecord>();
        try {
            FileInputStream fis = ctx.openFileInput(FILE_NAME);
            Scanner scn = new Scanner(fis);
            while (scn.hasNextLine()) {
                String line = scn.nextLine();
                StoredRecord record = readRecord(line);
                if (record != null) recordList.add(record);
            }
        } catch (Exception e) {
            Log.e("DataChangeTracker.retrieveRecords()", e.getLocalizedMessage(), e);
        }
        return recordList;
    }

    private StoredRecord readRecord(String line) throws ClassNotFoundException, IOException {
        StoredRecord record = null;
        String[] array = line.split(":", 3);
        char type = array[OPERATION_TYPE].charAt(0);
        Class storedClass = Class.forName(array[STORED_CLASS_NAME]);
        JSONBean bean = (JSONBean) mapper.readValue(array[JSON_BEAN], storedClass);
        record = new StoredRecord(type, bean);
        return record;
    }

    public void clearRecords() {
        try {
            FileOutputStream fos = ctx.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            PrintStream out = new PrintStream(fos);
            out.close();
        } catch (Exception e) {
            Log.e("DataChangeTracker.clearRecords()", e.getLocalizedMessage(), e);
        }
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

}

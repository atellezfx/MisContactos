package com.acme.miscontactos.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.acme.miscontactos.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Esta clase es utilizada para administrar la creación y actualización de tu base de datos.
 * Esta clase usualmente proporciona las clases DAO (Patrón de diseño Data Access Object)
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "agenda.db";
    private static final int DATABASE_VERSION = 1;

    // Objetos DAO que se utilizarán para acceder a la tabla Contacto
    private Dao<Contacto, Integer> contactoDAO = null;
    private RuntimeExceptionDao<Contacto, Integer> contactoRuntimeDAO = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * Método invocado cundo la base de datos es creada. Usualmente se hacen llamadas a los métodos
     * createTable para crear las tablas que almacenarán los datos
     *
     * @param db
     * @param source
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource source) {
        try {
            Log.i(DatabaseHelper.class.getSimpleName(), "onCreate()");
            TableUtils.createTable(source, Contacto.class);
        } catch (SQLException ex) {
            Log.e(DatabaseHelper.class.getSimpleName(), "Imposible crear la base de datos", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Este método es invocado cuando a aplicación es actualizada y tiene un número de versión
     * superior. Permite el ajuste a los mdatos para alinearse con la nueva versión
     *
     * @param db
     * @param source
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource source, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getSimpleName(), "onUpgrade()");
            TableUtils.dropTable(source, Contacto.class, true);
            // Después de eliminar las tablas anteriores, creamos nuevamente la base de datos
            onCreate(db, source);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getSimpleName(), "Imposible eliminad la base de datos", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtiene el objeto Data Access Object (DAO) para la entidad Contacto
     *
     * @return
     * @throws SQLException
     */
    public Dao<Contacto, Integer> getContactoDAO() throws SQLException {
        if (contactoDAO == null) contactoDAO = getDao(Contacto.class);
        return contactoDAO;
    }

    /**
     * Obtiene la versión RuntimeException del objeto DAO para la entidad Contacto.
     * Los objetos RuntimeExceptionDao únicamente arrojan excepciones de tipo RuntimeException
     *
     * @return
     */
    public RuntimeExceptionDao<Contacto, Integer> getContactoRuntimeDAO() {
        if (contactoRuntimeDAO == null) contactoRuntimeDAO = getRuntimeExceptionDao(Contacto.class);
        return contactoRuntimeDAO;
    }

    /**
     * Cierra las conexiones a la base de datos
     */
    @Override
    public void close() {
        super.close();
        contactoDAO = null;
        contactoRuntimeDAO = null;
    }
}

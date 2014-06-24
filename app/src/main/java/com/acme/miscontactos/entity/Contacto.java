package com.acme.miscontactos.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcel;
import android.provider.BaseColumns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

import static com.tojc.ormlite.android.annotation.AdditionalAnnotation.Contract;
import static com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultContentMimeTypeVnd;
import static com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultContentUri;
import static com.tojc.ormlite.android.annotation.AdditionalAnnotation.DefaultSortOrder;

@Contract
@DefaultContentUri(authority = "com.acme.miscontactos", path = "contacto")
@DefaultContentMimeTypeVnd(name = "com.acme.miscontactos.provider", type = "contacto")
@DatabaseTable(tableName = "contacto")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contacto extends JSONBean {

    @JsonProperty
    @DatabaseField
    private int serverId;

    @DefaultSortOrder
    @JsonProperty("androidId")
    @DatabaseField(columnName = BaseColumns._ID, generatedId = true, allowGeneratedIdInsert = true)
    private int id; // Primary Key

    @JsonProperty
    @DatabaseField(index = true, canBeNull = false)
    private String nombre;

    @JsonProperty
    @DatabaseField
    private String telefono;

    @JsonProperty
    @DatabaseField
    private String email;

    @JsonProperty
    @DatabaseField
    private String direccion;

    @JsonProperty
    @DatabaseField
    private String imageUri;

    @JsonProperty
    @DatabaseField
    private String propietario;

    /**
     * El motor de ORMLite requiere este constructor vacío para poder instanciar objetos de
     * esta clase por medio del API de Reflection
     */
    public Contacto() {
    }

    public Contacto(int id, String nombre, String telefono, String email, String direccion, String imageUri, String propietario) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.imageUri = imageUri;
        this.propietario = propietario;
        procesarHashMD5();
    }

    public Contacto(String nombre, String telefono, String email, String direccion, String imageUri, String propietario) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.imageUri = imageUri;
        this.propietario = propietario;
        procesarHashMD5();
    }

    public Contacto(ContentValues values) {
        this.id = values.getAsInteger(ContactoContract._ID);
        this.serverId = values.getAsInteger(ContactoContract.SERVERID);
        this.nombre = values.getAsString(ContactoContract.NOMBRE);
        this.telefono = values.getAsString(ContactoContract.TELEFONO);
        this.email = values.getAsString(ContactoContract.EMAIL);
        this.direccion = values.getAsString(ContactoContract.DIRECCION);
        this.imageUri = values.getAsString(ContactoContract.IMAGEURI);
        this.propietario = values.getAsString(ContactoContract.PROPIETARIO);
        this.procesarHashMD5();
    }

    public static ArrayList<Contacto> crearListaDeCursor(Cursor cursor) {
        ArrayList<Contacto> lista = new ArrayList<Contacto>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ContentValues values = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, values);
                Contacto contacto = new Contacto(values);
                contacto.procesarHashMD5();
                cursor.moveToNext();
                lista.add(contacto);
            }
        }
        return lista;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ContactoContract._ID, id);
        values.put(ContactoContract.SERVERID, serverId);
        values.put(ContactoContract.NOMBRE, nombre);
        values.put(ContactoContract.TELEFONO, telefono);
        values.put(ContactoContract.EMAIL, email);
        values.put(ContactoContract.DIRECCION, direccion);
        values.put(ContactoContract.IMAGEURI, imageUri);
        values.put(ContactoContract.PROPIETARIO, propietario);
        values.put("md5", md5);
        return values;
    }

    //<editor-fold desc="CODIGO DE SOPORTE A INTERFAZ PARCELABLE">
    public static final Creator<Contacto> CREATOR = new Creator<Contacto>() {
        @Override
        public Contacto createFromParcel(Parcel in) {
            return new Contacto(in);
        }

        @Override
        public Contacto[] newArray(int size) {
            return new Contacto[size];
        }
    };

    public Contacto(Parcel in) {
        this.serverId = in.readInt();
        this.id = in.readInt();
        this.nombre = in.readString();
        this.telefono = in.readString();
        this.email = in.readString();
        this.direccion = in.readString();
        this.imageUri = in.readString();
        this.propietario = in.readString();
        procesarHashMD5();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(serverId);
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(telefono);
        dest.writeString(email);
        dest.writeString(direccion);
        dest.writeString(imageUri);
        dest.writeString(propietario);
    }
    //</editor-fold>

    //<editor-fold desc="GETTER METHODS">
    public int getId() {
        return id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getPropietario() {
        return propietario;
    }

    public int getServerId() {
        return serverId;
    }
    //</editor-fold>

    //<editor-fold desc="SETTER METHODS">
    public void setId(int id) {
        int oldVal = this.id;
        this.id = id;
        support.firePropertyChange("id", oldVal, id);
    }

    public void setNombre(String nombre) {
        String oldVal = this.nombre;
        this.nombre = nombre;
        support.firePropertyChange("nombre", oldVal, nombre);
    }

    public void setTelefono(String telefono) {
        String oldVal = this.telefono;
        this.telefono = telefono;
        support.firePropertyChange("telefono", oldVal, telefono);
    }

    public void setEmail(String email) {
        String oldVal = this.email;
        this.email = email;
        support.firePropertyChange("email", oldVal, email);
    }

    public void setDireccion(String direccion) {
        String oldVal = this.direccion;
        this.direccion = direccion;
        support.firePropertyChange("direccion", oldVal, direccion);
    }

    public void setServerId(int serverId) {
        int oldVal = this.serverId;
        this.serverId = serverId;
        support.firePropertyChange("serverId", oldVal, serverId);
    }

    public void setImageUri(String imageUri) {
        String oldVal = this.imageUri;
        this.imageUri = imageUri;
        support.firePropertyChange("imageUri", oldVal, imageUri);
    }

    public void setPropietario(String propietario) {
        String oldVal = this.propietario;
        this.propietario = propietario;
        support.firePropertyChange("propietario", oldVal, propietario);
    }
    //</editor-fold>

    //<editor-fold desc="METODOS EQUALS Y HASHCODE">
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contacto contacto = (Contacto) o;

        if (id != contacto.id) return false;
        if (direccion != null ? !direccion.equals(contacto.direccion) : contacto.direccion != null)
            return false;
        if (email != null ? !email.equals(contacto.email) : contacto.email != null) return false;
        if (imageUri != null ? !imageUri.equals(contacto.imageUri) : contacto.imageUri != null)
            return false;
        if (nombre != null ? !nombre.equals(contacto.nombre) : contacto.nombre != null)
            return false;
        if (propietario != null ? !propietario.equals(contacto.propietario) : contacto.propietario != null)
            return false;
        if (telefono != null ? !telefono.equals(contacto.telefono) : contacto.telefono != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (nombre != null ? nombre.hashCode() : 0);
        result = 31 * result + (telefono != null ? telefono.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (direccion != null ? direccion.hashCode() : 0);
        result = 31 * result + (imageUri != null ? imageUri.hashCode() : 0);
        result = 31 * result + (propietario != null ? propietario.hashCode() : 0);
        return result;
    }
    //</editor-fold>
}

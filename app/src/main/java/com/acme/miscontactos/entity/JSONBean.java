package com.acme.miscontactos.entity;

import android.content.ContentValues;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.j256.ormlite.field.DatabaseField;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Created by alejandro on 05/06/14.
 */
public abstract class JSONBean implements PropertyChangeListener, Parcelable {

    /**
     * Propiedad para identificar cambios en el bean, para proceder a la sincronización
     */
    @JsonProperty
    @DatabaseField
    protected String md5;

    protected JSONBean() {
        support.addPropertyChangeListener(this);
    }

    //<editor-fold desc="PROPERTY CHANGE SUPPORT">
    @JsonIgnore
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    //</editor-fold>

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        procesarHashMD5();
    }

    public abstract int getServerId();
    public abstract void setServerId(int serverId);

    public abstract ContentValues getContentValues();

    public String getMd5() {
        return md5;
    }

    public void procesarHashMD5() {
        HashFunction hf = Hashing.md5();
        // El hashcode se calcula a partir de todas las propiedades del bean
        HashCode code = hf.hashInt(hashCode());
        md5 = code.toString();
    }

}

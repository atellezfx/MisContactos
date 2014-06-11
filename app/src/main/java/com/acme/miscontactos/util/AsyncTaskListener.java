package com.acme.miscontactos.util;

/**
 * Created by alejandro on 6/10/14.
 */
public interface AsyncTaskListener<T> {

    void processResult(T result);
}

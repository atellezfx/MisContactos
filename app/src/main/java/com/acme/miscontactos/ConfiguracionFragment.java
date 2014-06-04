package com.acme.miscontactos;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by alejandro on 04/06/14.
 */
public class ConfiguracionFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.configuracion);
    }
}

package com.acme.miscontactos;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by alejandro on 04/06/14.
 */
public class ConfiguracionActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ConfiguracionFragment configFrag = new ConfiguracionFragment();
        transaction.replace(android.R.id.content, configFrag);
        transaction.commit();
    }
}

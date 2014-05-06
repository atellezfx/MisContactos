package com.acme.miscontactos;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.acme.miscontactos.util.DatabaseHelper;
import com.acme.miscontactos.util.TabsPagerAdapter;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper>
        implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

    // Control de fichas (tabs)
    private ViewPager viewPager;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializarTabs();
    }

    private void inicializarTabs() {
        View view = findViewById(R.id.pager); // El mismo id carga el xml de tablet y el de phone
        String viewTag = String.valueOf(view.getTag());
        Log.d(getClass().getSimpleName(), String.format("Layout: %s", viewTag));

        if (viewTag.equals("phone")) {
            viewPager = (ViewPager) findViewById(R.id.pager);
            actionBar = getActionBar();
            TabsPagerAdapter adapter = new TabsPagerAdapter(getFragmentManager());

            viewPager.setAdapter(adapter);
            actionBar.setHomeButtonEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Agregando las fichas (tabs)
            String[] titulos = {"Crear Contacto", "Lista Contactos"};
            for (String nombre : titulos) {
                ActionBar.Tab tab = actionBar.newTab().setText(nombre);
                tab.setTabListener(this);
                actionBar.addTab(tab);
            }
            viewPager.setOnPageChangeListener(this);
        }

    }

    //<editor-fold desc="METODOS TAB CHANGE LISTENER">
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
    //</editor-fold>

    //<editor-fold desc="METODOS VIEW CHANGE LISTENER">
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    //</editor-fold>
}

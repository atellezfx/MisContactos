package com.acme.miscontactos.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.acme.miscontactos.CrearContactoFragment;
import com.acme.miscontactos.ListaContactosFragment;

/**
 * Created by alejandro on 5/2/14.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CrearContactoFragment();
            case 1:
                return new ListaContactosFragment();
        }
        return null;
    }
}

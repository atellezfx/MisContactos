package mx.vainiyasoft.agenda;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getListView().setSystemUiVisibility(
                    // Igual, la siguiente línea oculta el ActionBar, la comentamos
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    // Las siguientes dos líneas ocultan la barra de navegación, en nuestra app
                    // puede traer problemas, pues sí resulta últi. Las comentamos también.
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}

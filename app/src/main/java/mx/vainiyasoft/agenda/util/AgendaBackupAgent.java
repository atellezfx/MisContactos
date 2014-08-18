package mx.vainiyasoft.agenda.util;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.util.Log;

/**
 * Created by alejandro on 18/08/14.
 */
public class AgendaBackupAgent extends BackupAgentHelper {

    private final String LOG_TAG = AgendaBackupAgent.class.getSimpleName();
    private final String SHARED_PREFERENCES_KEY = "shared_preferences";

    @Override
    public void onCreate() {
        Context context = getApplicationContext();
        StringBuilder sb = new StringBuilder(context.getPackageName());
        sb.append("_preferences");
        SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(context, sb.toString());
        addHelper(SHARED_PREFERENCES_KEY, prefsHelper);
        Log.d(LOG_TAG, String.format("ARCHIVO RESPLADADO: %s", sb.toString()));
    }
}

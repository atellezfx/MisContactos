package mx.vainiyasoft.agenda.entity;

import com.tojc.ormlite.android.OrmLiteSimpleContentProvider;
import com.tojc.ormlite.android.framework.MatcherController;
import com.tojc.ormlite.android.framework.MimeTypeVnd;

import mx.vainiyasoft.agenda.util.DatabaseHelper;

/**
 * Created by alejandro on 6/23/14.
 */
public class ContactoProvider extends OrmLiteSimpleContentProvider<DatabaseHelper> {

    @Override
    protected Class<DatabaseHelper> getHelperClass() {
        return DatabaseHelper.class;
    }

    @Override
    public boolean onCreate() {
        MatcherController controller = new MatcherController();
        controller.add(Contacto.class, MimeTypeVnd.SubType.DIRECTORY, "", ContactoContract.CONTENT_URI_PATTERN_MANY);
        controller.add(Contacto.class, MimeTypeVnd.SubType.ITEM, "#", ContactoContract.CONTENT_URI_PATTERN_ONE);
        setMatcherController(controller);
        return true;
    }
}

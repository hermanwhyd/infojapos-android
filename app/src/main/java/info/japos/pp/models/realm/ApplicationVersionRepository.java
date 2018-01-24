package info.japos.pp.models.realm;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import info.japos.pp.models.ApplicationInfo.ApplicationInfo;
import io.realm.Realm;

/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public class ApplicationVersionRepository {

    private static ApplicationVersionRepository instance;
    private final Realm realm;

    public ApplicationVersionRepository(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static ApplicationVersionRepository with(Fragment fragment) {
        if (instance == null) {
            instance = new ApplicationVersionRepository(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static ApplicationVersionRepository with(Activity activity) {
        if (instance == null) {
            instance = new ApplicationVersionRepository(activity.getApplication());
        }
        return instance;
    }

    public static ApplicationVersionRepository with(Application application) {
        if (instance == null) {
            instance = new ApplicationVersionRepository(application);
        }
        return instance;
    }

    public static ApplicationVersionRepository getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    //Refresh the realm istance
    public void refresh() {
        realm.refresh();
    }
    
    public ApplicationInfo getVersion() {
        return realm.where(ApplicationInfo.class).findFirst();
    }

    public void saveOrUpdate(ApplicationInfo ai) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(ApplicationInfo.class);
                realm.copyToRealm(ai);
            }
        });
    }
}

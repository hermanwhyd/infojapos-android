package info.japos.pp.models.realm;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by HWAHYUDI on 30-Dec-17.
 */

public class EnumsRepository {
    private static EnumsRepository instance;
    private final Realm realm;

    public EnumsRepository(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static EnumsRepository with(Fragment fragment) {
        if (instance == null) {
            instance = new EnumsRepository(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static EnumsRepository with(Activity activity) {
        if (instance == null) {
            instance = new EnumsRepository(activity.getApplication());
        }
        return instance;
    }

    public static EnumsRepository with(Application application) {
        if (instance == null) {
            instance = new EnumsRepository(application);
        }
        return instance;
    }

    public static EnumsRepository getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    //Refresh the realm istance
    public void refresh() {
        realm.refresh();
    }

    public RealmResults<Enums> getEnumsByGrup(String grup) {
        return realm.where(Enums.class)
                .equalTo("grup", grup)
                .findAll();
    }

    public void addEnums(List<Enums> enums) {
        realm.executeTransaction((realm) -> realm.copyToRealmOrUpdate(enums));
    }

    public void deleteAllEnums() {
        realm.executeTransaction((Realm realm) -> realm.clear(Enums.class));
    }
}

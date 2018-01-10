package info.japos.pp.models.realm;

import info.japos.pp.models.ApplicationInfo.ApplicationInfo;
import io.realm.Realm;

/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public class ApplicationVersionRepository {
    public ApplicationInfo getVersion() {
        ApplicationInfo ai;
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        ai = realm.where(ApplicationInfo.class).findFirst();
        realm.commitTransaction();
        return ai;
    }

    public void saveOrUpdate(ApplicationInfo ai) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(ApplicationInfo.class);
        realm.copyToRealm(ai);
        realm.commitTransaction();
    }
}

package info.japos.pp.models.realm;

import android.media.effect.EffectUpdateListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import info.japos.pp.AppController;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by HWAHYUDI on 30-Dec-17.
 */

public class EnumsRepository {
    public RealmResults<Enums> getEnumsByGrup(String grup) {
        RealmResults<Enums> result;
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        result = r.where(Enums.class)
                .equalTo("grup", grup)
                .findAll();
        r.commitTransaction();

        return result;
    }

    public void addEnums(List<Enums> enums) {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        r.copyToRealmOrUpdate(enums);
        r.commitTransaction();
    }

    public void deleteAllEnums() {
        Realm r = Realm.getDefaultInstance();
        r.beginTransaction();
        RealmResults<Enums> results = r.where(Enums.class).findAll();
        results.deleteAllFromRealm();
        r.commitTransaction();
    }
}

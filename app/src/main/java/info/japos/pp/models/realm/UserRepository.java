package info.japos.pp.models.realm;

import info.japos.pp.models.User;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by HWAHYUDI on 03-Jan-18.
 */

public class UserRepository {
    /**
     * get single result User
     * @param id
     * @return
     */
    public User getUser(Integer id) {
        User user;
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        user = realm.where(User.class).equalTo("id", id).findFirst();
        realm.commitTransaction();

        return user;
    }

    public void AddUser(User user) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }

    public void removeUser(User user) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<User> users = realm.where(User.class).equalTo("id", user.getId()).findAll();
        user.deleteFromRealm();
        realm.commitTransaction();
    }
}

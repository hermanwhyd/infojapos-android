package info.japos.pp.models.realm;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import info.japos.pp.models.User;
import io.realm.Realm;

/**
 * Created by HWAHYUDI on 03-Jan-18.
 */

public class UserRepository {

    private static UserRepository instance;
    private final Realm realm;

    public UserRepository(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static UserRepository with(Fragment fragment) {
        if (instance == null) {
            instance = new UserRepository(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static UserRepository with(Activity activity) {
        if (instance == null) {
            instance = new UserRepository(activity.getApplication());
        }
        return instance;
    }

    public static UserRepository with(Application application) {
        if (instance == null) {
            instance = new UserRepository(application);
        }
        return instance;
    }

    public static UserRepository getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    //Refresh the realm istance
    public void refresh() {
        realm.refresh();
    }
    
    /**
     * get single result User
     * @param id
     * @return
     */
    public User getUser(Integer id) {
        return realm.where(User.class).equalTo("id", id).findFirst();
    }

    public void AddUser(User user) {
        realm.executeTransaction((Realm realm) -> realm.copyToRealmOrUpdate(user));
    }

    public void removeUser(User user) {
        realm.executeTransaction((Realm realm) -> user.removeFromRealm());
    }
}

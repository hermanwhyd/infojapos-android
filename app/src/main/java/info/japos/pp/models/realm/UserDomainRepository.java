package info.japos.pp.models.realm;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import java.util.List;

import io.realm.Realm;

public class UserDomainRepository {
    private static UserDomainRepository instance;
    private final Realm realm;

    public UserDomainRepository(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static UserDomainRepository with(Fragment fragment) {
        if (instance == null) {
            instance = new UserDomainRepository(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static UserDomainRepository with(Activity activity) {
        if (instance == null) {
            instance = new UserDomainRepository(activity.getApplication());
        }
        return instance;
    }

    public static UserDomainRepository with(Application application) {
        if (instance == null) {
            instance = new UserDomainRepository(application);
        }
        return instance;
    }

    public static UserDomainRepository getInstance() {
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
    public UserDomain getUserDomain(Integer id) {
        return realm.where(UserDomain.class).equalTo("id", id).findFirst();
    }

    public List<UserDomain> getAllUserDomain() {
        return realm.allObjects(UserDomain.class);
    }

    public void AddUserDomain(UserDomain user) {
        realm.executeTransaction((Realm realm) -> realm.copyToRealmOrUpdate(user));
    }

    public void removeUserDomain(UserDomain user) {
        realm.executeTransaction((Realm realm) -> user.removeFromRealm());
    }
}

package info.japos.pp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import info.japos.pp.models.User;
import info.japos.pp.models.realm.UserRepository;

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared preferences file name
    private static final String PREF_NAME = "info.japos.pp.SessionManager";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userIdLogged";
    private static final String KEY_API_TOKEN = "apiToken";

    // Shared Preferences
    private SharedPreferences pref;
    private Editor editor;
    private Context _context;
    private UserRepository userRepository;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        userRepository = new UserRepository();
    }

    public void setUserLogged(int userId) {
        editor.putInt(KEY_USER_ID, userId);

        // commit changes
        editor.commit();

        Log.d(TAG, "User logged session modified!");
    }

    public int getUserIdLogged() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public User getUserLoged() {
        if (!isLoggedIn()) return null;

        int userId = getUserIdLogged();
        return userRepository.getUser(userId);
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Invalidate logged session
     * set is login to False
     * remove api token
     * remove user logged from realm
     */
    public void invalidate() {
        userRepository.removeUser(getUserLoged());
        setLogin(Boolean.FALSE);
        removeApiToken();
        removeUserIdLogged();
    }

    public void setApiToken(String token) {
        editor.putString(KEY_API_TOKEN, token);
        editor.commit();
    }

    private void removeApiToken() {
        editor.remove(KEY_API_TOKEN);
        editor.commit();
    }

    public void removeUserIdLogged() {
        editor.remove(KEY_USER_ID);
        editor.commit();
    }

    public String getApiToken() {
        return pref.getString(KEY_API_TOKEN, "");
    }
}

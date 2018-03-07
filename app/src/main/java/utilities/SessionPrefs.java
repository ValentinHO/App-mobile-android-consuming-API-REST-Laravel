package utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import models.User;

/**
 * Created by valentin on 04/03/2018.
 */

public class SessionPrefs {

    public static final String PREFS_NAME = "USER_PREFS";
    public static final String PREF_USER_ID = "PREF_USER_ID";
    public static final String PREF_USER_NAME = "PREF_USER_NAME";
    public static final String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    public static final String PREF_USER_TOKEN = "PREF_USER_TOKEN";

    private final SharedPreferences mPrefs;

    private boolean mIsLoggedIn = false;

    private static SessionPrefs INSTANCE;

    public static SessionPrefs get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionPrefs(context);
        }
        return INSTANCE;
    }

    private SessionPrefs(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mIsLoggedIn = !TextUtils.isEmpty(mPrefs.getString(PREF_USER_TOKEN, null));
    }

    public boolean isLoggedIn() {
        return mIsLoggedIn;
    }

    public void saveUser(User user) {
        if (user != null) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt(PREF_USER_ID, user.getId());
            editor.putString(PREF_USER_NAME, user.getName());
            editor.putString(PREF_USER_EMAIL, user.getEmail());
            editor.putString(PREF_USER_TOKEN, user.getToken());
            editor.apply();

            mIsLoggedIn = true;
        }
    }

    public void logOut(){
        mIsLoggedIn = false;
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_USER_ID, null);
        editor.putString(PREF_USER_NAME, null);
        editor.putString(PREF_USER_EMAIL, null);
        editor.putString(PREF_USER_TOKEN, null);
        editor.apply();
    }
}

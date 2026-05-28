package com.posmix.mixtuvgag.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "micropos_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    public void setLogin(boolean isLoggedIn, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        if (username != null) {
            editor.putString(KEY_USERNAME, username);
        }
        editor.apply();
    }
    
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }
    
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    public void checkLogin() {
        if (!isLoggedIn()) {
            android.content.Intent intent = new android.content.Intent(context, com.posmix.mixtuvgag.activities.LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
}

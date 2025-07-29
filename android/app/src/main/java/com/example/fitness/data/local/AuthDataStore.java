package com.example.fitness.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthDataStore {
    private static final String PREFS_NAME = "auth_preferences";
    private static final String JWT_TOKEN_KEY = "jwt_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_ROLE_KEY = "user_role";

    private final SharedPreferences sharedPreferences;

    @Inject
    public AuthDataStore(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthData(String token, String userId, String email, String name, String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(JWT_TOKEN_KEY, token);
        editor.putString(USER_ID_KEY, userId);
        editor.putString(USER_EMAIL_KEY, email);
        editor.putString(USER_NAME_KEY, name);
        if (role != null) {
            editor.putString(USER_ROLE_KEY, role);
        }
        editor.apply();
    }

    public String getJwtToken() {
        return sharedPreferences.getString(JWT_TOKEN_KEY, "");
    }

    public String getUserId() {
        return sharedPreferences.getString(USER_ID_KEY, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(USER_EMAIL_KEY, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(USER_NAME_KEY, "");
    }

    public String getUserRole() {
        return sharedPreferences.getString(USER_ROLE_KEY, "");
    }

    public boolean isLoggedIn() {
        String token = getJwtToken();
        return token != null && !token.isEmpty();
    }

    public void clearAuthData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(JWT_TOKEN_KEY);
        editor.remove(USER_ID_KEY);
        editor.remove(USER_EMAIL_KEY);
        editor.remove(USER_NAME_KEY);
        editor.remove(USER_ROLE_KEY);
        editor.apply();
    }
}

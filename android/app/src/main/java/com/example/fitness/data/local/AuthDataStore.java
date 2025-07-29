package com.example.fitness.data.local;

import android.content.Context;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthDataStore {
    private static final String DATASTORE_NAME = "auth_preferences";
    private static final Preferences.Key<String> JWT_TOKEN_KEY = PreferencesKeys.stringKey("jwt_token");
    private static final Preferences.Key<String> USER_ID_KEY = PreferencesKeys.stringKey("user_id");
    private static final Preferences.Key<String> USER_EMAIL_KEY = PreferencesKeys.stringKey("user_email");
    private static final Preferences.Key<String> USER_NAME_KEY = PreferencesKeys.stringKey("user_name");
    private static final Preferences.Key<String> USER_ROLE_KEY = PreferencesKeys.stringKey("user_role");

    private final RxDataStore<Preferences> dataStore;

    @Inject
    public AuthDataStore(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, DATASTORE_NAME).build();
    }

    public Single<Void> saveAuthData(String token, String userId, String email, String name, String role) {
        return dataStore.updateDataAsync(prefs -> {
            androidx.datastore.preferences.core.MutablePreferences mutablePrefs = prefs.toMutablePreferences();
            mutablePrefs.set(JWT_TOKEN_KEY, token != null ? token : "");
            mutablePrefs.set(USER_ID_KEY, userId != null ? userId : "");
            mutablePrefs.set(USER_EMAIL_KEY, email != null ? email : "");
            mutablePrefs.set(USER_NAME_KEY, name != null ? name : "");
            if (role != null) {
                mutablePrefs.set(USER_ROLE_KEY, role);
            }
            return Single.just(mutablePrefs);
        }).ignoreElement().toSingleDefault((Void) null);
    }

    public Flowable<String> getJwtToken() {
        return dataStore.data().map(prefs -> prefs.get(JWT_TOKEN_KEY) != null ? prefs.get(JWT_TOKEN_KEY) : "");
    }

    public Single<String> getJwtTokenSync() {
        return getJwtToken().firstOrError();
    }

    public Flowable<String> getUserId() {
        return dataStore.data().map(prefs -> prefs.get(USER_ID_KEY) != null ? prefs.get(USER_ID_KEY) : "");
    }

    public Single<String> getUserIdSync() {
        return getUserId().firstOrError();
    }

    public Flowable<String> getUserEmail() {
        return dataStore.data().map(prefs -> prefs.get(USER_EMAIL_KEY) != null ? prefs.get(USER_EMAIL_KEY) : "");
    }

    public Single<String> getUserEmailSync() {
        return getUserEmail().firstOrError();
    }

    public Flowable<String> getUserName() {
        return dataStore.data().map(prefs -> prefs.get(USER_NAME_KEY) != null ? prefs.get(USER_NAME_KEY) : "");
    }

    public Single<String> getUserNameSync() {
        return getUserName().firstOrError();
    }

    public Flowable<String> getUserRole() {
        return dataStore.data().map(prefs -> prefs.get(USER_ROLE_KEY) != null ? prefs.get(USER_ROLE_KEY) : "");
    }

    public Single<String> getUserRoleSync() {
        return getUserRole().firstOrError();
    }

    public Flowable<Boolean> isLoggedIn() {
        return getJwtToken().map(token -> token != null && !token.isEmpty());
    }

    public Single<Boolean> isLoggedInSync() {
        return isLoggedIn().firstOrError();
    }

    public Single<Void> clearAuthData() {
        return dataStore.updateDataAsync(prefs -> {
            androidx.datastore.preferences.core.MutablePreferences mutablePrefs = prefs.toMutablePreferences();
            mutablePrefs.remove(JWT_TOKEN_KEY);
            mutablePrefs.remove(USER_ID_KEY);
            mutablePrefs.remove(USER_EMAIL_KEY);
            mutablePrefs.remove(USER_NAME_KEY);
            mutablePrefs.remove(USER_ROLE_KEY);
            return Single.just(mutablePrefs);
        }).ignoreElement().toSingleDefault((Void) null);
    }
}

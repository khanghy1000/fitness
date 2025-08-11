package com.example.fitness.data.network;

import android.util.Log;
import com.example.fitness.data.local.AuthDataStore;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import io.reactivex.rxjava3.schedulers.Schedulers;

/** Captures the better-auth.session_token cookie and stores it for socket auth. */
@Singleton
public class SessionCookieJar implements CookieJar {
    private static final String TAG = "SessionCookieJar";
    private final AuthDataStore authDataStore;

    @Inject
    public SessionCookieJar(AuthDataStore authDataStore) {
        this.authDataStore = authDataStore;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies.isEmpty()) {
            Log.d(TAG, "No cookies in response for " + url);
        }
        for (Cookie cookie : cookies) {
            Log.d(TAG, "Cookie: name=" + cookie.name() + " domain=" + cookie.domain());
            if ("better-auth.session_token".equals(cookie.name()) || cookie.name().endsWith("session_token")) {
                Log.d(TAG, "Captured session cookie len=" + cookie.value().length());
                authDataStore.updateToken(cookie.value())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        () -> Log.d(TAG, "Token persisted"),
                        throwable -> Log.e(TAG, "Failed to persist token", throwable)
                    );
            } 
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // We rely on bearer/JWT header elsewhere; returning empty list keeps behavior explicit.
        return new ArrayList<>();
    }
}

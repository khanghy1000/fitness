package com.example.fitness.di;

import com.example.fitness.Constants;
import com.example.fitness.data.local.AuthDataStore;
import com.example.fitness.data.network.*;
import com.example.fitness.data.network.adapter.BigDecimalAdapter;
import com.example.fitness.data.network.retrofit.AuthApi;
import com.example.fitness.data.network.retrofit.ConnectionsApi;
import com.example.fitness.data.network.retrofit.ExercisesApi;
import com.example.fitness.data.network.retrofit.NutritionApi;
import com.example.fitness.data.network.retrofit.PlannedWorkoutsApi;
import com.example.fitness.data.network.retrofit.UsersApi;
import com.example.fitness.data.network.retrofit.WorkoutsApi;
import com.example.fitness.model.Message;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides
    @Singleton
    public Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new BigDecimalAdapter())
                .addLast(new KotlinJsonAdapterFactory())
                .build();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor, SessionCookieJar sessionCookieJar) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
        
    builder.addInterceptor(authInterceptor);
    builder.cookieJar(sessionCookieJar);

        // Network-level logging for Set-Cookie diagnostics
        builder.addNetworkInterceptor(chain -> {
            okhttp3.Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);
            java.util.List<String> setCookies = response.headers("Set-Cookie");
            if (!setCookies.isEmpty()) {
                android.util.Log.d("HTTP_COOKIE", "URL=" + request.url() + " Set-Cookie count=" + setCookies.size());
                for (String c : setCookies) {
                    android.util.Log.d("HTTP_COOKIE", "  -> " + c);
                }
            } else {
                android.util.Log.d("HTTP_COOKIE", "URL=" + request.url() + " (no Set-Cookie)");
            }
            return response;
        });

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(Moshi moshi, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    @Provides
    @Singleton
    public Socket provideSocket(AuthDataStore authDataStore) {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.timeout = 10000;
            options.auth = new java.util.HashMap<String, String>();
            try {
                String authToken = authDataStore.getJwtTokenSync().blockingGet();
                if (authToken != null && !authToken.isEmpty()) {
                    // For newer Socket.IO protocol versions (server >= v3) "auth" object works.
                    options.auth.put("token", authToken);
                    // For older Java client library (2.x) which may not send auth payload, also append as query param.
                    options.query = "token=" + java.net.URLEncoder.encode(authToken, java.nio.charset.StandardCharsets.UTF_8.name());
                }
                if (options.auth.isEmpty()) {
                    // If no auth token is available, we can still create the socket
                    // The server will handle authentication failure
                    System.out.println("No auth token available for socket connection.");
                }
                System.out.println("Socket auth token: " + options.auth.get("token"));
            } catch (Exception e) {
                // Log the error but continue without auth token
                // The socket will handle authentication failure
                System.err.println("Failed to get auth token for socket: " + e.getMessage());
            }
            return IO.socket(Constants.BASE_URL, options); // Add SOCKET_URL to Constants
        } catch (Exception e) {
            throw new RuntimeException("Failed to create socket", e);
        }
    }

    @Provides
    @Singleton
    public JsonAdapter<Message> provideMessageAdapter(Moshi moshi) {
        return moshi.adapter(Message.class);
    }

    @Provides
    @Singleton
    public JsonAdapter<List<Message>> provideMessageListAdapter(Moshi moshi) {
        Type listType = Types.newParameterizedType(List.class, Message.class);
        return moshi.adapter(listType);
    }
    @Provides
    @Singleton
    public SocketService provideSocketService(Socket socket,
                                              JsonAdapter<Message> messageAdapter,
                                              JsonAdapter<List<Message>> messageListAdapter,
                                              Moshi moshi,
                                              AuthDataStore authDataStore) {
        return new SocketService(socket, messageAdapter, messageListAdapter, moshi, authDataStore);
    }

    @Provides
    @Singleton
    public AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }

    @Provides
    @Singleton
    public ConnectionsApi provideConnectionsApi(Retrofit retrofit) {
        return retrofit.create(ConnectionsApi.class);
    }

    @Provides
    @Singleton
    public UsersApi provideUsersApi(Retrofit retrofit) {
        return retrofit.create(UsersApi.class);
    }

    @Provides
    @Singleton
    public ExercisesApi provideExercisesApi(Retrofit retrofit) {
        return retrofit.create(ExercisesApi.class);
    }

    @Provides
    @Singleton
    public NutritionApi provideNutritionApi(Retrofit retrofit) {
        return retrofit.create(NutritionApi.class);
    }

    @Provides
    @Singleton
    public WorkoutsApi provideWorkoutsApi(Retrofit retrofit) {
        return retrofit.create(WorkoutsApi.class);
    }

    @Provides
    @Singleton
    public PlannedWorkoutsApi providePlannedWorkoutsApi(Retrofit retrofit) {
        return retrofit.create(PlannedWorkoutsApi.class);
    }
}

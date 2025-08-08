package com.example.fitness.di;

import com.example.fitness.Constants;
import com.example.fitness.data.network.*;
import com.example.fitness.data.network.adapter.BigDecimalAdapter;
import com.example.fitness.data.network.retrofit.AuthApi;
import com.example.fitness.data.network.retrofit.ConnectionsApi;
import com.example.fitness.data.network.retrofit.ExercisesApi;
import com.example.fitness.data.network.retrofit.NutritionApi;
import com.example.fitness.data.network.retrofit.PlannedWorkoutsApi;
import com.example.fitness.data.network.retrofit.UsersApi;
import com.example.fitness.data.network.retrofit.WorkoutsApi;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
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
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
        
        // Add auth interceptor
        builder.addInterceptor(authInterceptor);

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(Moshi moshi, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(Constants.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
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

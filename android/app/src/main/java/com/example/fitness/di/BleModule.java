package com.example.fitness.di;

import android.content.Context;
import com.example.fitness.ble.BleServiceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class BleModule {
    
    @Provides
    @Singleton
    public BleServiceManager provideBleServiceManager(@ApplicationContext Context context) {
        return new BleServiceManager(context);
    }
}

package com.rm.loginappcompose.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.rm.loginappcompose.data.model.UserInfo
import com.rm.loginappcompose.data.model.UserSerializer
import com.rm.loginappcompose.util.KeyStoreUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    private val Context.datastore by dataStore(
        fileName = "user-info.json",
        serializer = UserSerializer(KeyStoreUtil("userAuth"))
    )

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<UserInfo> = context.datastore
}
package com.rm.loginappcompose.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.rm.loginappcompose.data.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfoDataStoreRepository @Inject constructor(
    private val dataStore: DataStore<UserInfo>
) {
    val data: Flow<UserInfo> = dataStore.data

    suspend fun updateUserInfo(
        name: String,
        email: String,
        picture: String,
        mongoAccessId: String
    ) = withContext(Dispatchers.IO) {
        dataStore.updateData {
            UserInfo(
                name = name,
                email = email,
                picture = picture,
                mongoAccessId = mongoAccessId
            )
        }
    }
}
package com.rm.loginappcompose.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.rm.loginappcompose.data.UserInfoDataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userInfoDataStoreRepository: UserInfoDataStoreRepository
) : ViewModel() {

    suspend fun getUserInfo() = withContext(Dispatchers.IO) {
        val userInfo = userInfoDataStoreRepository.data.first()
        Log.d("userInfo", "${userInfo.name}")
        Log.d("userInfo", "${userInfo.email}")
        Log.d("userInfo", "${userInfo.picture}")
        Log.d("userInfo", "${userInfo.mongoAccessId}")
    }
}
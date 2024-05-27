package com.rm.loginappcompose.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rm.loginappcompose.data.UserInfoDataStoreRepository
import com.rm.loginappcompose.data.model.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userInfoDataStoreRepository: UserInfoDataStoreRepository
) : ViewModel() {

    private val _userInfo: MutableStateFlow<UserInfo> = MutableStateFlow(UserInfo())
    val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

    init {
        getUserInfo()
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            userInfoDataStoreRepository.data
                .catch {
                    Log.d(TAG, "${it.printStackTrace()}")
                }
                .collect { userInfo ->
                    Log.d(TAG, "${userInfo.name}, ${userInfo.email}")
                    _userInfo.update { userInfo }
                }
        }
    }

    companion object {
        const val TAG = "home"
    }
}
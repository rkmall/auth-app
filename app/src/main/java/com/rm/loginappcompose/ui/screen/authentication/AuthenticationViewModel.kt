package com.rm.loginappcompose.ui.screen.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rm.loginappcompose.data.AppConstants.MONGO_APP_ID
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthenticationViewModel : ViewModel() {

    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    fun setLoadingState(loading: Boolean) {
        _loadingState.update { loading }
    }

    fun signInWithMongoDb(
        tokenId: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            Log.d("Token", "Signing in to MongoDb...")
            try {
                val result: User = withContext(Dispatchers.IO) {
                    App.create(MONGO_APP_ID).login(
                        Credentials.google(tokenId, GoogleAuthType.ID_TOKEN )
                    )
                }

                withContext(Dispatchers.Main) {
                    onSuccess(result.loggedIn)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

}
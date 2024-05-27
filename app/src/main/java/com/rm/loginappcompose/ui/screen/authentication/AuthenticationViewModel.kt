package com.rm.loginappcompose.ui.screen.authentication

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rm.loginappcompose.data.AppConstants.MONGO_APP_ID
import com.rm.loginappcompose.data.UserInfoDataStoreRepository
import com.rm.loginappcompose.googlesignin.GoogleSignInState
import com.rm.loginappcompose.googlesignin.GoogleUser
import com.rm.loginappcompose.googlesignin.getUserFromTokenId
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val userInfoDataStoreRepository: UserInfoDataStoreRepository
) : ViewModel() {

    private val _authState : MutableStateFlow<AuthState> = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authEvent: MutableSharedFlow<AuthEvent> = MutableSharedFlow()

    private val _authEffect: Channel<AuthEffect> = Channel()
    val authEffect: Flow<AuthEffect> = _authEffect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            _authEvent.collect { event ->
                handleEvents(event)
            }
        }
    }

    private fun setAuthState(reducer: AuthState.() -> AuthState) {
        val newState = authState.value.reducer()
        _authState.value = newState
    }

    fun setAuthEvent(authEvent: AuthEvent) {
        viewModelScope.launch {
            _authEvent.emit(authEvent)
        }
    }

    private fun setEffect(builder: () -> AuthEffect) {
        val effectValue = builder()
        viewModelScope.launch {
            _authEffect.send(effectValue)
        }
    }

    private fun handleEvents(authEvent: AuthEvent) {
        when(authEvent) {
            is AuthEvent.OnSignInButtonClicked -> {
                authenticateWithGoogleSignIn()
            }

            is AuthEvent.OnTokenReceived -> {
                signInToMongoDb(authEvent.tokenId)
            }

            is AuthEvent.OnLoginDialogDismissed -> {
                setEffect {
                    AuthEffect.OnSignInDismissed
                }
            }
        }
    }

    private fun authenticateWithGoogleSignIn() {
        setAuthState {
            googleButtonState.signInState.open()
            copy(isLoading = true)
        }
    }

    private fun signInToMongoDb(tokenId: String) {
        viewModelScope.launch {
            val googleUser = getUserFromTokenId(tokenId)
            Log.d("Token", "Signing in to MongoDb...")
            try {
                val mongoUser = withContext(Dispatchers.IO) {
                    App.create(MONGO_APP_ID).login(Credentials.jwt(tokenId))

                }

                saveUserInfo(googleUser, mongoUser)

                withContext(Dispatchers.Main) {
                    setAuthState {
                        copy(
                            googleButtonState = googleButtonState.copy(isAuthenticated = mongoUser.loggedIn),
                            isLoading = false,
                            isError = false
                        )
                    }

                    if (authState.value.googleButtonState.isAuthenticated) {
                        setEffect { AuthEffect.Navigation.ToHomeScreen }
                    } else {
                        setEffect { AuthEffect.OnSignInFailure }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setAuthState {
                        copy(
                            googleButtonState = googleButtonState.copy(isAuthenticated = false),
                            isLoading = false,
                            isError = true
                        )
                    }

                    setEffect { AuthEffect.OnSignInFailure }
                }
            }
        }
    }

    private suspend fun saveUserInfo(
        googleUser: GoogleUser?,
        mongoUser: User
    ) = withContext(Dispatchers.IO) {
        userInfoDataStoreRepository.updateUserInfo(
            name = googleUser?.fullName ?: "na",
            email = googleUser?.email ?: "na",
            picture = googleUser?.picture ?: "na",
            mongoAccessId = mongoUser.accessToken
        )
    }
}

sealed class AuthEvent {
    data object OnSignInButtonClicked : AuthEvent()
    data class OnTokenReceived(val tokenId: String) : AuthEvent()
    data class OnLoginDialogDismissed(val message: String) : AuthEvent()
}

data class AuthState(
    val googleButtonState: GoogleButtonState = GoogleButtonState(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

data class GoogleButtonState(
    val isAuthenticated: Boolean = false,
    val signInState: GoogleSignInState = GoogleSignInState(false)
)

sealed class AuthEffect {
    data object OnSignInFailure : AuthEffect()
    data object OnSignInDismissed : AuthEffect()

    sealed class Navigation : AuthEffect() {
        data object ToHomeScreen : Navigation()
    }
}

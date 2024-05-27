package com.rm.loginappcompose.ui.screen.authentication

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val userInfoDataStoreRepository: UserInfoDataStoreRepository
) : ViewModel() {

    private val _authState : MutableStateFlow<AuthState> = MutableStateFlow(setInitialState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authEvent: MutableSharedFlow<AuthEvent> = MutableSharedFlow()

    private val _authEffect: Channel<AuthEffect> = Channel()
    val authEffect: Flow<AuthEffect> = _authEffect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    private fun setInitialState(): AuthState {
        val user = App.create(MONGO_APP_ID).currentUser
        return if (user != null && user.loggedIn) {
            AuthState(
                googleButtonState = GoogleButtonState(isAuthenticated = true, signInState = GoogleSignInState(false)),
                isLoading = false,
                isError = false
            )
        } else {
            AuthState()
        }
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

    private fun setAuthEffect(builder: () -> AuthEffect) {
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

            is AuthEvent.OnGoogleSignInDismissed -> {
                setAuthEffect {
                    AuthEffect.GoogleSignInDismissed(authEvent.message)
                }
            }

            is AuthEvent.OnGoogleSignInException -> {
                setAuthState {
                    copy(isLoading = false)
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

    /**
     * Mongodb will use the google tokenId to exchange User information from the Google server.
     * Then, those user information will be stored in the Mongodb.
     */
    private fun signInToMongoDb(tokenId: String) {
        viewModelScope.launch {
            val googleUser = getUserFromTokenId(tokenId)

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
                        setAuthEffect { AuthEffect.Navigation.ToHomeScreen }
                    } else {
                        setAuthEffect { AuthEffect.SignInFailure }
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
                    setAuthEffect { AuthEffect.SignInFailure }
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

    private fun handleGoogleSignInException() {
        setAuthState {
            copy(isLoading = false)
        }
    }
}

sealed class AuthEvent {
    data object OnSignInButtonClicked : AuthEvent()
    data class OnTokenReceived(val tokenId: String) : AuthEvent()
    data class OnGoogleSignInDismissed(val message: String) : AuthEvent()
    data object OnGoogleSignInException : AuthEvent()
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
    data object SignInFailure : AuthEffect()
    data class GoogleSignInDismissed(val message: String) : AuthEffect()

    sealed class Navigation : AuthEffect() {
        data object ToHomeScreen : Navigation()
    }
}

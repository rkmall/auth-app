package com.rm.loginappcompose.googlesignin

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rm.loginappcompose.ui.screen.authentication.AuthEvent
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "googleSignIn"

@Composable
fun SignInWithGoogle(
    state: GoogleSignInState,
    clientId: String,
    rememberAccount: Boolean = false,
    onTokenIdReceived: (String) -> Unit,
    onDialogDismissed: (String) -> Unit,
    onExceptionReceived: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val googleIdOption: GetGoogleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(rememberAccount)
            .setServerClientId(clientId)
            .setNonce(getNonce())
            .build()
    }

    val request: GetCredentialRequest = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    LaunchedEffect(key1 = state.isOpen) {
        if (state.isOpen) {
            scope.launch {
                try {
                    val response = credentialManager.getCredential(
                        request = request,
                        context = context
                    )

                    handleSignIn(
                        credentialResponse = response,
                        onTokenIdReceived = { token ->
                            onTokenIdReceived(token)
                            state.close()
                        },
                        onSignInFailure =  { dismissMessage ->
                            onDialogDismissed(dismissMessage)
                            state.close()
                        }
                    )
                } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                    when (e) {
                        is NoCredentialException -> {
                            handleNoCredentialException(
                                context,
                                state,
                                onExceptionReceived,
                                onDialogDismissed
                            )
                        }

                        is GetCredentialCancellationException -> {
                            Log.e(TAG, "${e.message}")
                            onDialogDismissed("Dialog closed!")
                            state.close()
                            onExceptionReceived()
                        }

                        else -> {
                            Log.e(TAG, "${e.message}")
                            onDialogDismissed("Unknown error occurred!")
                            state.close()
                            onExceptionReceived()
                        }
                    }
                }
            }
        }
    }
}

private fun handleSignIn(
    credentialResponse: GetCredentialResponse,
    onTokenIdReceived: (String) -> Unit,
    onSignInFailure: (String) -> Unit
) {
    when (val credential = credentialResponse.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    onTokenIdReceived(googleIdTokenCredential.idToken)
                } catch (e: GoogleIdTokenParsingException) {
                    onSignInFailure("Invalid Google tokenId response: ${e.message}" )
                }
            } else {
                onSignInFailure("Unexpected type of Credential")
            }
        }

        else -> onSignInFailure("Unexpected type of Credential")
    }
}

private fun handleNoCredentialException(
    context: Context,
    state: GoogleSignInState,
    onExceptionReceived: () -> Unit,
    onSignInFailure: (String) -> Unit
) {
    try {
        // Navigate to the activity that allows to add new Google account
        val addAccountIntent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        }
        state.close()
        onExceptionReceived()
        context.startActivity(addAccountIntent)
    } catch (e: Exception) {
        Log.e(TAG, "${e.message}")
        onSignInFailure("Error while opening Settings for Google Accounts")
        state.close()
    }
}

private fun getNonce(): String {
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

@Composable
fun rememberGoogleSignInState(): GoogleSignInState {
    return rememberSaveable(
        saver = GoogleSignInStateSaver
    ) {
        GoogleSignInState()
    }
}

private val GoogleSignInStateSaver: Saver<GoogleSignInState, Boolean> = Saver(
    save = { state -> state.isOpen },
    restore = { isOpen -> GoogleSignInState(isOpen) }
)

class GoogleSignInState(open: Boolean = false ) {
    var isOpen by mutableStateOf(open)
        private set

    fun open() {
        isOpen = true
    }

    internal fun close() {
        isOpen = false
    }
}

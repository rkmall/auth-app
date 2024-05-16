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
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "googleSignIn"

@Composable
fun SignInWithGoogle(
    state: GoogleSignInState,
    clientId: String,
    rememberAccount: Boolean = true,
    onTokenIdReceived: (String) -> Unit,
    onDialogDismissed: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val googleIdOption: GetGoogleIdOption = remember {
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(rememberAccount)
            .setServerClientId(clientId)
            .setNonce(createNonce())
            .build()
    }

    val request: GetCredentialRequest = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    LaunchedEffect(key1 = state.opened) {
        if (state.opened) {
            scope.launch {
                try {
                    val response = credentialManager.getCredential(
                        request = request,
                        context = context
                    )

                    handleSignIn(
                        credentialResponse = response,
                        onTokenIdReceived = {
                            onTokenIdReceived(it)
                            state.close()
                        },
                        onDialogDismissed =  {
                            onDialogDismissed(it)
                            state.close()
                        }
                    )
                } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                    when (e) {
                        is NoCredentialException -> handleNoCredentialException(context, state, onDialogDismissed)

                        is GetCredentialCancellationException -> {
                            Log.e(TAG, "${e.message}")
                            onDialogDismissed("Dialog closed!")
                            state.close()
                        }

                        else -> {
                            Log.e(TAG, "${e.message}")
                            onDialogDismissed("Unknown error occurred!")
                            state.close()
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
    onDialogDismissed: (String) -> Unit
) {
    when (val credential = credentialResponse.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    onTokenIdReceived(googleIdTokenCredential.idToken)
                } catch (e: GoogleIdTokenParsingException) {
                    onDialogDismissed("Invalid Google tokenId response: ${e.message}" )
                }
            } else {
                onDialogDismissed("Unexpected type of Credential")
            }
        }

        else -> onDialogDismissed("Unexpected type of Credential")
    }
}

private fun handleNoCredentialException(
    context: Context,
    state: GoogleSignInState,
    onDialogDismissed: (String) -> Unit
) {
    try {
        val addAccountIntent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
            putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
        }
        state.close()
        context.startActivity(addAccountIntent)
    } catch (e: Exception) {
        Log.e(TAG, "${e.message}")
        onDialogDismissed("Error while opening Settings for Google Accounts")
        state.close()
    }
}

private fun createNonce(): String {
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
    save = { state -> state.opened },
    restore = { opened -> GoogleSignInState(opened) }
)

class GoogleSignInState(open: Boolean = false ) {
    var opened by mutableStateOf(open)
        private set

    fun open() {
        opened = true
    }

    internal fun close() {
        opened = false
    }
}

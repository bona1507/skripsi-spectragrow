package com.pkmkcub.spectragrow.view.ui.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.pkmkcub.spectragrow.BuildConfig
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private var credentialManager: CredentialManager? = null
) : ViewModel() {

    private fun initializeCredentialManager(context: Context) {
        credentialManager = CredentialManager.create(context)
    }

    fun login(email: String, password: String, loginCallback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loginCallback(task.isSuccessful)
            }
    }

    fun forgotPassword(email: String, forgotPasswordCallback: (Boolean) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                forgotPasswordCallback(task.isSuccessful)
            }
    }

    fun register(email: String, password: String, registrationCallback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                registrationCallback(task.isSuccessful)
            }
    }

    fun getDisplayNameUser(): String {
        return auth.currentUser?.displayName ?: "Pengguna"
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun logout(logoutCallback: (Boolean) -> Unit) {
        auth.signOut()
        logoutCallback(true)
    }

    private fun generateHashedNonce(): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(UUID.randomUUID().toString().toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun handleError(context: Context, message: String, exception: Exception) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.e("AuthViewModel", message, exception)
    }

    fun handleGoogleAuth(context: Context, navCallback: () -> Unit) {
        initializeCredentialManager(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setNonce(generateHashedNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager?.getCredential(context, request)
                val credential = result?.credential
                val googleIdTokenCredential = credential?.let {
                    GoogleIdTokenCredential.createFrom(it.data)
                }

                googleIdTokenCredential?.let { tokenCredential ->
                    firebaseAuthWithGoogle(context, tokenCredential.idToken, navCallback)
                }
            } catch (e: Exception) {
                handleError(context, "Google Sign-In failed. Please try again.", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(context: Context, idToken: String, navCallback: () -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "signInWithCredential:success")
                    navCallback()
                } else {
                    Log.w("AuthViewModel", "signInWithCredential:failure", task.exception)
                    task.exception?.let {
                        handleError(context, "Authentication failed. Please try again.",
                            it
                        )
                    }
                }
            }
    }
}

package com.pkmkcub.spectragrow.view.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.pkmkcub.spectragrow.BuildConfig
import com.pkmkcub.spectragrow.R
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    var credentialManager: CredentialManager? = null
) : ViewModel() {

    fun initializeCredentialManager(context: Context) {
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

    fun handleGoogleAuth(context: Context, activity: androidx.fragment.app.FragmentActivity, navController: NavController) {
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
                    GoogleIdTokenCredential.createFrom(
                        it.data)
                }
                if (googleIdTokenCredential != null) {
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken, activity, navController)
                }
            } catch (e: GetCredentialException) {
                handleError(context, "Failed to retrieve credentials. Please try again.", e)
            } catch (e: GoogleIdTokenParsingException) {
                handleError(context, "Failed to parse Google ID Token. Please try again.", e)
            } catch (e: Exception) {
                handleError(context, "An unexpected error occurred. Please try again.", e)
            }
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

    fun generateHashedNonce(): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(UUID.randomUUID().toString().toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun handleError(context: Context, message: String, exception: Exception) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.e("AuthViewModel", message, exception)
    }

    private fun firebaseAuthWithGoogle(idToken: String, activity: androidx.fragment.app.FragmentActivity, navController: NavController) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "signInWithCredential:success")
                    if (navController.currentDestination?.id == R.id.onboardingFragment) {
                        navController.navigate(R.id.action_onboarding_to_home)
                    } else if (navController.currentDestination?.id == R.id.mapsFragment) {
                        navController.navigate(R.id.action_maps_to_home)
                    }
                } else {
                    Log.w("AuthViewModel", "signInWithCredential:failure", task.exception)
                }
            }
    }
}

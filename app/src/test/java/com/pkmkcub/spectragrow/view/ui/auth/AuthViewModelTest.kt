package com.pkmkcub.spectragrow.view.ui.auth

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.credentials.CredentialManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pkmkcub.spectragrow.MainCoroutinesRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutinesRule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var credentialManager: CredentialManager

    @Mock
    private lateinit var firebaseUser: FirebaseUser

    @Mock
    private lateinit var mockSignInTask: Task<AuthResult>

    @Mock
    private lateinit var mockSendPasswordResetEmailTask: Task<Void>

    @Mock
    private lateinit var mockCreateUserTask: Task<AuthResult>

    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        authViewModel = AuthViewModel(auth, credentialManager)

        `when`(auth.signInWithEmailAndPassword(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mockSignInTask)
        `when`(auth.sendPasswordResetEmail(Mockito.anyString()))
            .thenReturn(mockSendPasswordResetEmailTask)
        `when`(auth.createUserWithEmailAndPassword(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(mockCreateUserTask)
    }

    @Test
    fun testInitializeCredentialManager() {
        authViewModel.initializeCredentialManager(context)
        Assert.assertNotNull(authViewModel.credentialManager)
    }

    @Test
    fun testLogin() {
        val email = "test@example.com"
        val password = "password"
        val loginCallback: (Boolean) -> Unit = mock()

        authViewModel.login(email, password, loginCallback)
        verify(auth).signInWithEmailAndPassword(eq(email), eq(password))
        verify(mockSignInTask).addOnCompleteListener(Mockito.any())
    }

    @Test
    fun testForgotPassword() {
        val email = "test@example.com"
        val forgotPasswordCallback: (Boolean) -> Unit = mock()

        authViewModel.forgotPassword(email, forgotPasswordCallback)
        verify(auth).sendPasswordResetEmail(eq(email))
        verify(mockSendPasswordResetEmailTask).addOnCompleteListener(Mockito.any())
    }

    @Test
    fun testRegister() {
        val email = "test@example.com"
        val password = "password"
        val registrationCallback: (Boolean) -> Unit = mock()

        authViewModel.register(email, password, registrationCallback)
        verify(auth).createUserWithEmailAndPassword(eq(email), eq(password))
        verify(mockCreateUserTask).addOnCompleteListener(Mockito.any())
    }

    @Test
    fun testGetDisplayNameUser() {
        `when`(auth.currentUser).thenReturn(firebaseUser)
        `when`(firebaseUser.displayName).thenReturn("Test User")

        val displayName = authViewModel.getDisplayNameUser()
        Assert.assertEquals("Test User", displayName)
    }

    @Test
    fun testGetCurrentUser() {
        `when`(auth.currentUser).thenReturn(firebaseUser)
        val currentUser = authViewModel.getCurrentUser()
        Assert.assertEquals(firebaseUser, currentUser)
    }

    @Test
    fun testLogout() {
        val logoutCallback: (Boolean) -> Unit = mock()

        authViewModel.logout(logoutCallback)
        verify(auth).signOut()
        verify(logoutCallback).invoke(true)
    }

    private inline fun <reified T> mock(): T = Mockito.mock(T::class.java)
}

package com.pkmkcub.spectragrow.view.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentLoginBinding
import com.pkmkcub.spectragrow.view.ui.auth.LoginFragmentDirections

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        binding.apply {
            btnLogin.setOnClickListener {
                handleLogin()
            }
            forgotPw.setOnClickListener {
                handleForgotPassword()
            }
            tvRegister.setOnClickListener {
                findNavController().navigate(LoginFragmentDirections.actionLoginToRegister())
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        when {
            email.isEmpty() || password.isEmpty() -> {
                showToast(R.string.field_empty)
            }
            !binding.etEmail.isEmailValid -> {
                showToast(R.string.incorrect_email_format)
            }
            !binding.etPassword.isPasswordValid -> {
                showToast(R.string.incorrect_pw_format)
            }
            !binding.snkCheck.isChecked -> {
                showToast(R.string.snk_uncheck)
            }
            else -> {
                binding.progressBar.isVisible = true
                viewModel.login(email, password) { success ->
                    binding.progressBar.isVisible = false
                    if (success) {
                        navigateToHome()
                    } else {
                        showToast(R.string.email_password_incorrect)
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        Handler(Looper.getMainLooper()).post {
            if (isAdded) {
                val action = LoginFragmentDirections.actionLoginToHome()
                findNavController().navigate(action)
            } else {
                showToast(R.string.login_fragment_title)
            }
        }
    }


    private fun handleForgotPassword() {
        val email = binding.etEmail.text.toString()
        if (email.isEmpty()) {
            showToast(R.string.field_empty)
        } else {
            viewModel.forgotPassword(email) { success ->
                val messageResId = if (success) R.string.forgot_pw_success else R.string.forgot_pw_unsuccess
                showToast(messageResId)
            }
        }
    }

    private fun showToast(messageResId: Int) {
        context?.let {
            Toast.makeText(it, getString(messageResId), Toast.LENGTH_SHORT).show()
        }
    }
}

package com.pkmkcub.spectragrow.view.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        binding.apply {
            btnRegister.setOnClickListener {
                if (binding.etEmail.text.toString().isEmpty() && binding.etPassword.text.toString().isEmpty() && binding.etUsername.text.toString().isEmpty() && binding.etConfirmPassword.text.toString().isEmpty()) {
                    showToast(R.string.field_empty)
                } else {
                    handleEmailRegistration()
                }
            }
            tvLogin.setOnClickListener {
                findNavController().navigate(R.id.action_register_to_login)
            }
        }
    }

    private fun handleEmailRegistration() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val username = binding.etUsername.text.toString()
        when {
            !binding.etEmail.isEmailValid -> {
                showToast(R.string.incorrect_email_format)
                return
            }
            !binding.etPassword.isPasswordValid -> {
                showToast(R.string.incorrect_pw_format)
                return
            }
            !binding.snkCheck.isChecked -> {
                showToast(R.string.snk_uncheck)
                return
            }
            email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty() -> {
                showToast(R.string.field_empty)
                return
            }
            password != confirmPassword -> {
                showToast(R.string.password_not_match)
                return
            }
            else -> {
                binding.progressBar.isVisible = true
                viewModel.register(email, password) { success ->
                    binding.progressBar.isVisible = false
                    if (success) {
                        findNavController().navigate(R.id.loginFragment)
                    } else {
                        showToast(R.string.register_failed)
                    }
                }
            }
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}
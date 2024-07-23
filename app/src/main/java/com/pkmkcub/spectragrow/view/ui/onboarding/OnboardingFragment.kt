package com.pkmkcub.spectragrow.view.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pkmkcub.spectragrow.R
import com.pkmkcub.spectragrow.databinding.FragmentOnboardingBinding
import com.pkmkcub.spectragrow.view.ui.auth.AuthViewModel

class OnboardingFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initializeCredentialManager(requireContext())
        if (viewModel.getCurrentUser() == null) {
            setupUi()
        } else {
            findNavController().navigate(R.id.action_onboarding_to_home)
        }
    }

    private fun setupUi() {
        binding.apply {
            logBtn.setOnClickListener {
                findNavController().navigate(R.id.action_onboarding_to_login)
            }
            regBtn.setOnClickListener {
                findNavController().navigate(R.id.action_onboarding_to_register)
            }
            googleBtn.setOnClickListener {
                viewModel.handleGoogleAuth(requireContext(), requireActivity(), findNavController())
            }
            skipBtn.setOnClickListener {
                onboardingFragment.transitionToState(R.id.tabs_ob2)
                onboardingFragment.transitionToState(R.id.tabs_ob3)
            }
        }
    }
}

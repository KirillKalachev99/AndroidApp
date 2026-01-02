package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R.*
import com.example.ansteducation.activity.AppActivity
import com.example.ansteducation.databinding.AuthFragmentBinding
import com.example.ansteducation.viewModel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : Fragment() {
    private lateinit var binding: AuthFragmentBinding

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AuthFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading
                -> {
                    showLoading(true)
                    binding.loginInput.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    showLoading(false)
                    findNavController().popBackStack()
                }
                is AuthViewModel.AuthState.Error -> {
                    showLoading(false)
                    binding.loginButton.isEnabled = true
                    showError(state.message)
                }
                else -> {
                    showLoading(false)
                    binding.loginButton.isEnabled = true
                }
            }
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val login = binding.loginInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (login.isBlank() || password.isBlank()) {
                showError("Please enter login and password")
                return@setOnClickListener
            }

            viewModel.authenticate(login, password)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.loginButton.text = if (show) getString(string.loading) else getString(string.log_in)
        binding.loginButton.isEnabled = !show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}


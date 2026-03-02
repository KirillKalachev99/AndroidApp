package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.databinding.FragmentRegisterBinding
import com.example.ansteducation.viewModel.AuthViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private val maxSizePx = 2048

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data
            if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(requireContext(), R.string.failed_photo, Toast.LENGTH_SHORT).show()
            } else if (uri != null) {
                viewModel.changeAvatar(uri, uri.toFile())
            }
        }

        viewModel.avatar.observe(viewLifecycleOwner) { photo ->
            if (photo != null) {
                binding.avatarPreview.setImageURI(photo.uri)
            } else {
                binding.avatarPreview.setImageResource(R.drawable.ic_avatar_placeholder_48)
            }
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.registerButton.isEnabled = false
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.registerButton.isEnabled = true
                    findNavController().popBackStack()
                }
                is AuthViewModel.AuthState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.registerButton.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.progressBar.isVisible = false
                    binding.registerButton.isEnabled = true
                }
            }
        }

        binding.selectAvatarButton.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .maxResultSize(maxSizePx, maxSizePx)
                .createIntent { intent ->
                    imagePickerLauncher.launch(intent)
                }
        }

        binding.registerButton.setOnClickListener {
            val login = binding.loginInput.text.toString()
            val name = binding.nameInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val repeatPassword = binding.repeatPasswordInput.text.toString()

            var hasError = false

            if (login.isBlank()) {
                binding.loginInput.error = getString(R.string.enter_your_login)
                hasError = true
            } else {
                binding.loginInput.error = null
            }

            if (name.isBlank()) {
                binding.nameInput.error = getString(R.string.enter_your_name)
                hasError = true
            } else {
                binding.nameInput.error = null
            }

            if (password.isBlank()) {
                binding.passwordInput.error = getString(R.string.enter_your_password)
                hasError = true
            } else {
                binding.passwordInput.error = null
            }

            if (repeatPassword.isBlank() || repeatPassword != password) {
                binding.repeatPasswordInput.error = getString(R.string.repeat_your_password)
                hasError = true
            } else {
                binding.repeatPasswordInput.error = null
            }

            if (viewModel.avatar.value == null) {
                Toast.makeText(requireContext(), "Выберите фото", Toast.LENGTH_SHORT).show()
                hasError = true
            }

            if (!hasError) {
                viewModel.register(login, name, password)
            }
        }
    }
}


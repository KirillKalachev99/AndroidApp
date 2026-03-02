package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.adapter.UsersAdapter
import com.example.ansteducation.databinding.FragmentUsersBinding
import com.example.ansteducation.fragment.UserProfileFragment.Companion.ARG_USER_ID
import com.example.ansteducation.fragment.UserProfileFragment.Companion.ARG_USER_LOGIN
import com.example.ansteducation.fragment.UserProfileFragment.Companion.ARG_USER_NAME
import com.example.ansteducation.viewModel.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding
    private val viewModel: UsersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.menu_users)

        val adapter = UsersAdapter { user ->
            val bundle = bundleOf(
                ARG_USER_ID to user.id,
                ARG_USER_NAME to user.name,
                ARG_USER_LOGIN to user.login,
            )
            findNavController().navigate(R.id.userProfileFragment, bundle)
        }
        binding.usersList.adapter = adapter

        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.items = users
            binding.errorText.isVisible = users.isEmpty()
            if (users.isEmpty()) {
                binding.errorText.text = getString(R.string.no_posts)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.errorText.isVisible = error != null
            binding.errorText.text = error ?: ""
        }

        if (savedInstanceState == null) {
            viewModel.loadUsers()
        }
    }
}


package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.example.ansteducation.R
import com.example.ansteducation.databinding.FragmentUserProfileBinding
import com.google.android.material.tabs.TabLayout
import com.example.ansteducation.viewModel.UserProfileHeaderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private val headerViewModel: UserProfileHeaderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireArguments().getLong(ARG_USER_ID)
        val userName = requireArguments().getString(ARG_USER_NAME).orEmpty()
        val userLogin = requireArguments().getString(ARG_USER_LOGIN).orEmpty()

        if (userName.isBlank() && userLogin.isBlank()) {
            headerViewModel.load(userId)
            headerViewModel.user.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    (activity as? AppCompatActivity)?.supportActionBar?.title =
                        "${user.name} (@${user.login})"
                }
            }
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                "$userName (@$userLogin)"
        }

        val tabWall = binding.tabLayout.newTab().setText(R.string.user_wall)
        val tabJobs = binding.tabLayout.newTab().setText(R.string.user_jobs)
        binding.tabLayout.addTab(tabWall, true)
        binding.tabLayout.addTab(tabJobs)

        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace(
                    R.id.user_profile_container,
                    UserWallFragment.newInstance(userId)
                )
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val fragment = when (tab.position) {
                    0 -> UserWallFragment.newInstance(userId)
                    else -> UserJobsFragment.newInstance(userId)
                }
                childFragmentManager.commit {
                    replace(R.id.user_profile_container, fragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    companion object {
        const val ARG_USER_ID = "userId"
        const val ARG_USER_NAME = "userName"
        const val ARG_USER_LOGIN = "userLogin"
    }
}


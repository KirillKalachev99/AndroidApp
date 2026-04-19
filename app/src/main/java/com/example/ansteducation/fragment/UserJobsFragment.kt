package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ansteducation.R
import com.example.ansteducation.adapter.JobsAdapter
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.databinding.FragmentUserJobsBinding
import com.example.ansteducation.fragment.UserProfileFragment.Companion.ARG_USER_ID
import com.example.ansteducation.viewModel.UserJobsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserJobsFragment : Fragment() {

    private lateinit var binding: FragmentUserJobsBinding
    private val viewModel: UserJobsViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireArguments().getLong(ARG_USER_ID)
        val currentUserId = appAuth.authState.value?.id
        val isMyProfile = currentUserId != null && currentUserId == userId

        val adapter = JobsAdapter(
            canEdit = isMyProfile,
            onDelete = { job ->
                if (isMyProfile) {
                    viewModel.deleteJob(job.id)
                }
            }
        )
        binding.jobsList.adapter = adapter

        binding.addJobFab.isVisible = isMyProfile
        if (isMyProfile) {
            binding.addJobFab.setOnClickListener {
                showAddJobDialog()
            }
        }

        viewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            adapter.items = jobs
            binding.errorText.isVisible = jobs.isEmpty() && !viewModel.loading.value!!
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.errorText.isVisible = error != null
            binding.errorText.text = error ?: ""
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        if (savedInstanceState == null) {
            if (isMyProfile) {
                viewModel.loadMyJobs()
            } else {
                viewModel.loadUserJobs(userId)
            }
        }
    }

    private fun showAddJobDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_job, null)
        val companyInput = dialogView.findViewById<EditText>(R.id.company_name_input)
        val positionInput = dialogView.findViewById<EditText>(R.id.position_input)
        val startInput = dialogView.findViewById<EditText>(R.id.start_date_input)
        val finishInput = dialogView.findViewById<EditText>(R.id.finish_date_input)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_job)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = companyInput.text.toString()
                val position = positionInput.text.toString()
                val start = startInput.text.toString()
                val finish = finishInput.text.toString().takeIf { it.isNotBlank() }

                if (name.isBlank() || position.isBlank() || start.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_empty_content),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    viewModel.addJob(name, position, start, finish)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        fun newInstance(userId: Long): UserJobsFragment =
            UserJobsFragment().apply {
                arguments = bundleOf(ARG_USER_ID to userId)
            }
    }
}


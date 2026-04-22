package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ansteducation.R
import com.example.ansteducation.adapter.PostCommentsAdapter
import com.example.ansteducation.databinding.FragmentPostCommentsBinding
import com.example.ansteducation.viewModel.PostCommentsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostCommentsFragment : Fragment() {

    private var _binding: FragmentPostCommentsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostCommentsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPostCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.post_comments_title)

        val adapter = PostCommentsAdapter()
        binding.commentsList.layoutManager = LinearLayoutManager(requireContext())
        binding.commentsList.adapter = adapter

        fun refreshEmptyState() {
            val loading = viewModel.loading.value == true
            val err = viewModel.error.value
            val list = viewModel.comments.value.orEmpty()
            binding.emptyText.isVisible = !loading && err.isNullOrBlank() && list.isEmpty()
        }

        viewModel.comments.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            refreshEmptyState()
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.isVisible = loading == true
            refreshEmptyState()
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            binding.errorText.isVisible = !err.isNullOrBlank()
            binding.errorText.text = err.orEmpty()
            refreshEmptyState()
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        binding.sendButton.setOnClickListener {
            viewModel.sendComment(binding.commentInput.text?.toString().orEmpty())
            binding.commentInput.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_POST_ID = "postId"
    }
}

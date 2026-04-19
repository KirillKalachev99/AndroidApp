package com.example.ansteducation.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.dto.Post
import com.example.ansteducation.util.SharePostWithRepostLauncher
import com.example.ansteducation.viewModel.AuthViewModel
import com.example.ansteducation.viewModel.PostViewModel
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.ansteducation.adapter.PostLoadingStateAdapter
import com.example.ansteducation.databinding.FragmentFeedBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var shareWithRepost: SharePostWithRepostLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareWithRepost = SharePostWithRepostLauncher(this) { viewModel.repost(it) }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val currentLocale = Locale.getDefault()
        val language = currentLocale.language

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) {
                viewModel.like(post)
            }

            override fun share(post: Post) {
                shareWithRepost.launch(post)
            }

            override fun onCommentsClick(post: Post) {
                findNavController().navigate(
                    R.id.action_global_postCommentsFragment,
                    bundleOf(PostCommentsFragment.ARG_POST_ID to post.id),
                )
            }

            override fun remove(post: Post) {
                viewModel.remove(post.id)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }

            override fun playVideo(videoUrl: String) {
                openVideoUrl(videoUrl)
            }

            override fun onPostClick(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_singlePostFragment,
                    bundleOf("postId" to post.id)
                )
            }

            override fun retryPost(post: Post) {
                viewModel.retryPost(post)
            }

            override fun onImageClick(post: Post, imageUrl: String) {
                val bundle = Bundle().apply {
                    putString("imageUrl", imageUrl)
                }
                findNavController().navigate(
                    R.id.action_feedFragment_to_fullscreenImageFragment,
                    bundle
                )
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() }
        )

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
            viewModel.refreshData()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { loadState ->
                binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading

                when {
                    loadState.refresh is LoadState.Error -> {
                        viewModel.onDataLoadError()
                    }
                    loadState.refresh is LoadState.NotLoading -> {
                        viewModel.onDataLoaded()
                    }
                    else -> Unit
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.apply {
                errorGroup.isVisible = state.error
                progress.isVisible = state.loading
            }

            if (state.error && !state.loading) {
                Snackbar.make(binding.root, R.string.no_response, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        binding.retry.setOnClickListener {
            adapter.refresh()
            viewModel.load()
        }

        binding.add.setOnClickListener {
            if (authViewModel.isAuthorized) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
                viewModel.clear()
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.auth_required_title)
                    .setMessage(R.string.auth_required_message)
                    .setPositiveButton(R.string.sign_in) { _, _ ->
                        findNavController().navigate(R.id.action_feedFragment_to_authFragment)
                    }
                    .setNeutralButton(R.string.sign_up) { _, _ ->
                        findNavController().navigate(R.id.registerFragment)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }

        binding.newerButton.setOnClickListener {
            adapter.refresh()
            viewModel.addNewer()
        }

        return binding.root
    }

    private fun openVideoUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.your_feed)
    }
}
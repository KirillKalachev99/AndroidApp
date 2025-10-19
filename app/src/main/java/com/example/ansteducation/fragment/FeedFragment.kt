package com.example.ansteducation.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.dto.Post
import com.example.ansteducation.viewModel.PostViewModel
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.databinding.FragmentFeedBinding
import com.example.ansteducation.model.FeedModel

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        setupAdapter()
        setupObservers()
        setupSwipeRefresh()
        setupAddButton()

        viewModel.load()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val horizontalPadding =
                systemBars.left + resources.getDimensionPixelSize(R.dimen.common_margin)
            val topPadding = systemBars.top + resources.getDimensionPixelSize(R.dimen.common_margin)
            val bottomPadding =
                systemBars.bottom + resources.getDimensionPixelSize(R.dimen.common_margin)

            v.setPadding(
                horizontalPadding,
                topPadding,
                horizontalPadding,
                bottomPadding
            )
            insets
        }
    }

    private fun setupAdapter() {
        adapter = PostAdapter(
            onInteractionListener = object : OnInteractionListener {
                override fun like(post: Post) {
                    viewModel.like(post)
                }

                override fun share(post: Post) {
                    sharePost(post)
                    viewModel.repost(post.id)
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
            },
            imgNames = emptyList()
        ) {
            // viewModel.view(it.id)
        }

        binding.list.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.imgNames.observe(viewLifecycleOwner) { imgNames ->
            adapter.updateImgNames(imgNames)
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            updateUI(state)
        }
    }

    private fun updateUI(state: FeedModel) {
        binding.apply {
            progress.isVisible = state.loading
            empty.isVisible = state.empty
            errorGroup.isVisible = state.error

            if (swipeRefresh.isRefreshing && !state.loading) {
                swipeRefresh.isRefreshing = false
            }
        }
        binding.retry.setOnClickListener {
            viewModel.load(slow = true)
        }
        val newPosts =
            state.posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
        if (newPosts) {
            binding.list.smoothScrollToPosition(0)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.load()
        }
    }

    private fun setupAddButton() {
        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            viewModel.clear()
        }
    }

    private fun sharePost(post: Post) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, post.content)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
    }

    private fun openVideoUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}
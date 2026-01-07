package com.example.ansteducation.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.dto.Post
import com.example.ansteducation.viewModel.PostViewModel
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.ansteducation.databinding.FragmentFeedBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val currentLocale = Locale.getDefault()
        val language = currentLocale.language


        val viewModel: PostViewModel by activityViewModels()


        val adapter = PostAdapter(object : OnInteractionListener {
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
        {
            //  viewModel.view(it.id)
        }

        binding.list.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

//        viewModel.data.observe(viewLifecycleOwner) { data ->
//            val previousSize = adapter.currentList.size
//            adapter.submitList(data.posts) {
//                if (data.posts.size > previousSize) {
//                    binding.list.scrollToPosition(0)
//                }
//            }
//            binding.apply {
//                empty.isVisible = data.empty
//            }
//        }

//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            println(it)
//            binding.apply {
//                newerButton.isVisible = it > 0
//                when (language) {
//                    "ru" -> newerButton.text = getString(R.string.newer_posts) + " " + it.toString()
//                    else -> newerButton.text = "Show $it new posts"
//                }
//            }
//        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.apply {
                errorGroup.isVisible = state.error
                swipeRefresh.isRefreshing = state.refreshing
            }

            if (state.error && !state.loading) {
                Snackbar.make(binding.root, R.string.no_response, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.retry.setOnClickListener {
            viewModel.load()
        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            viewModel.clear()
        }

        binding.newerButton.setOnClickListener {
            viewModel.addNewer()
        }

        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.your_feed)
    }
}
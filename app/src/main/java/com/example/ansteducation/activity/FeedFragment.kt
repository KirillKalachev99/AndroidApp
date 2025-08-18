package com.example.ansteducation.activity

import android.R.attr.text
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
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.viewModel.PostViewModel
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.activity.AppActivity.Companion.textArg
import com.example.ansteducation.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        val cardPostBinding = CardPostBinding.inflate(layoutInflater, binding.root, false)

        cardPostBinding.avatar.setImageResource(R.drawable.ic_netology_original_48dp)

        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { v, insets ->
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

        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) {
                viewModel.like(post.id)
            }

            override fun share(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
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
        }) {
            viewModel.view(it.id)
        }

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val new = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (new) binding.list.smoothScrollToPosition(0)
            }
        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            viewModel.clear()
        }

        return binding.root
    }

    private fun openVideoUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}


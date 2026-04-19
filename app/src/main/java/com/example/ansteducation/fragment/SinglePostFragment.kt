package com.example.ansteducation.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostViewHolder
import com.example.ansteducation.databinding.FragmentSinglePostBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.util.SharePostWithRepostLauncher
import com.example.ansteducation.util.formatApiDateTime
import com.example.ansteducation.viewModel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SinglePostFragment : Fragment() {

    private var _binding: FragmentSinglePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by activityViewModels()
    private lateinit var shareWithRepost: SharePostWithRepostLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareWithRepost = SharePostWithRepostLauncher(this) { viewModel.repost(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getLong("postId") ?: run {
            findNavController().navigateUp()
            return
        }

        val postVh = PostViewHolder(
            binding.singlePost,
            object : OnInteractionListener {
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
                    findNavController().navigateUp()
                }

                override fun edit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(R.id.action_singlePostFragment_to_newPostFragment)
                }

                override fun playVideo(url: String) {
                    openVideoUrl(url)
                }

                override fun retryPost(post: Post) {
                    viewModel.retryPost(post)
                }

                override fun onImageClick(post: Post, imageUrl: String) {
                    val bundle = Bundle().apply {
                        putString("imageUrl", imageUrl)
                    }
                    findNavController().navigate(
                        R.id.action_singlePostFragment_to_fullscreenImageFragment,
                        bundle
                    )
                }
            }
        )

        viewModel.singlePost.observe(viewLifecycleOwner) { post ->
            binding.progress.isVisible = post == null && viewModel.singlePostError.value == null
            if (post != null) {
                postVh.bind(post)
                bindExtras(post)
            }
        }

        viewModel.singlePostError.observe(viewLifecycleOwner) { err ->
            binding.errorText.isVisible = err != null
            binding.errorText.text = err.orEmpty()
            binding.progress.isVisible = err == null && viewModel.singlePost.value == null
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        viewModel.loadPost(postId)
    }

    private fun bindExtras(post: Post) {
        val jobText = post.authorJob?.takeIf { it.isNotBlank() }
        binding.authorJob.isVisible = jobText != null
        if (jobText != null) {
            binding.authorJob.text = jobText
        }

        val mentionLines = post.mentions.orEmpty()
        if (mentionLines.isNotEmpty()) {
            binding.mentionsLabel.isVisible = true
            binding.mentionsList.isVisible = true
            binding.mentionsList.text = mentionLines.joinToString("\n") { u ->
                "${u.name} (@${u.login})"
            }
        } else {
            binding.mentionsLabel.isVisible = false
            binding.mentionsList.isVisible = false
        }

        val link = post.link
        if (!link.isNullOrBlank()) {
            binding.postLink.isVisible = true
            binding.postLink.text = link
            binding.postLink.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
            }
        } else {
            binding.postLink.isVisible = false
        }

        val c = post.coords
        if (c != null) {
            binding.mapLink.isVisible = true
            binding.mapLink.setOnClickListener {
                val uri = Uri.parse("geo:${c.lat},${c.lon}?q=${c.lat},${c.lon}")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        } else {
            binding.mapLink.isVisible = false
        }

        binding.singlePost.published.text = formatApiDateTime(post.published)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openVideoUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}

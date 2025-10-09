package com.example.ansteducation.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostViewHolder
import com.example.ansteducation.databinding.FragmentSinglePostBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.viewModel.PostViewModel

class SinglePostFragment: Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSinglePostBinding.inflate(inflater, container, false)

        val postId = arguments?.getLong("postId") ?: findNavController().navigateUp()

        var postVh = PostViewHolder(binding.singlePost,
            object : OnInteractionListener {
                override fun like(post: Post) {
                    viewModel.like(post)
                }

                override fun share(post: Post) {
                    viewModel.repost(post.id)
                    sharePost(post)
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

            }, null)

            val post = viewModel.data.value?.posts?.find { it.id == postId }
            post?.let {
            postVh.bind(it)
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
}
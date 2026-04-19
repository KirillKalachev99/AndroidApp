package com.example.ansteducation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.databinding.FragmentUserWallBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.fragment.UserProfileFragment.Companion.ARG_USER_ID
import com.example.ansteducation.util.SharePostWithRepostLauncher
import com.example.ansteducation.viewModel.PostViewModel
import com.example.ansteducation.viewModel.UserWallViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserWallFragment : Fragment() {

    private lateinit var binding: FragmentUserWallBinding
    private val wallViewModel: UserWallViewModel by viewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private lateinit var shareWithRepost: SharePostWithRepostLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shareWithRepost = SharePostWithRepostLauncher(this) { postViewModel.repost(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserWallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireArguments().getLong(ARG_USER_ID)

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) {
                postViewModel.like(post)
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
                postViewModel.remove(post.id)
            }

            override fun edit(post: Post) {
                postViewModel.edit(post)
            }

            override fun playVideo(url: String) {
                // no-op here
            }

            override fun retryPost(post: Post) {
                postViewModel.retryPost(post)
            }

            override fun onImageClick(post: Post, imageUrl: String) {
                // no-op here for now
            }
        })

        binding.wallList.adapter = adapter

        wallViewModel.posts.observe(viewLifecycleOwner) { posts ->
            lifecycleScope.launch {
                adapter.submitData(PagingData.from(posts))
            }
        }

        wallViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.isVisible = isLoading
        }

        wallViewModel.error.observe(viewLifecycleOwner) { error ->
            binding.errorText.isVisible = error != null
            binding.errorText.text = error ?: ""
        }

        postViewModel.snackbarMessage.observe(viewLifecycleOwner) { msg ->
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        if (savedInstanceState == null) {
            wallViewModel.loadWall(userId)
        }
    }

    companion object {
        fun newInstance(userId: Long): UserWallFragment =
            UserWallFragment().apply {
                arguments = bundleOf(ARG_USER_ID to userId)
            }
    }
}


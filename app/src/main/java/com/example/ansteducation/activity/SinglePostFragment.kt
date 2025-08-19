package com.example.ansteducation.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostViewHolder
import com.example.ansteducation.adapter.onItemViewListener
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.databinding.FragmentSingleBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.viewModel.PostViewModel

class SinglePostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSingleBinding.inflate(inflater, container, false)

        PostViewHolder(
            binding.postCard,
            object : OnInteractionListener{

                override fun remove(post: Post) {
                    viewModel.remove(post.id)
                    findNavController().navigate(R.id.action_singlePostFragment_to_feedFragment)
                    viewModel.clear()
                }

                override fun edit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(R.id.action_singlePostFragment_to_newPostFragment)
                }
            },
            object : onItemViewListener{
                override fun invoke(post: Post) {
                    this.invoke(post)
                }
            })

        return binding.root
     }
}

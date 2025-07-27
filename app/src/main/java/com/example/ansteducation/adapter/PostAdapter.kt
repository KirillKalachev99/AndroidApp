package com.example.ansteducation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ansteducation.CountFormat
import com.example.ansteducation.R
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post


typealias onItemLikeListener = (post: Post) -> Unit
typealias onItemShareListener = (post: Post) -> Unit
typealias onItemViewListener = (post: Post) -> Unit


class PostAdapter(
    private val onItemLikeListener: onItemLikeListener,
    private val onItemShareListener: onItemShareListener,
    private val onItemViewListener: onItemViewListener? = null
) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onItemLikeListener, onItemShareListener, onItemViewListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
        holder.viewed(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onItemLikeListener: onItemLikeListener,
    private val onItemShareListener: onItemShareListener,
    private val onItemViewListener: onItemViewListener?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            avatar.setImageResource(R.drawable.ic_netology_original_48dp)
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = CountFormat.format(post.likes)
            shareCount.text = CountFormat.format(post.shares)
            seenCount.text = CountFormat.format(post.views)
            if (post.liked) {
                like.setImageResource(R.drawable.ic_liked_24)
            } else {
                like.setImageResource(R.drawable.ic_like_24)
            }
            like.setOnClickListener {
                onItemLikeListener(post)
            }
            share.setOnClickListener {
                onItemShareListener(post)
            }
        }
    }

    fun viewed(post: Post) {
        if (!post.viewedByMe) {
            itemView.post {
                onItemViewListener?.invoke(post)
            }
        }
    }
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
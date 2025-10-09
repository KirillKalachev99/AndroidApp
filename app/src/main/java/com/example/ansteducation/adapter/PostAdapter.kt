package com.example.ansteducation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ansteducation.CountFormat
import com.example.ansteducation.R
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post


interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun playVideo(url: String)
    fun onPostClick(post: Post){}
}

typealias onItemViewListener = (post: Post) -> Unit

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val onItemViewListener: onItemViewListener? = null,
) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, onItemViewListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
        holder.viewed(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val onItemViewListener: onItemViewListener?,
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("UseKtx")
    fun bind(post: Post) {

        binding.apply {
            avatar.setImageResource(R.drawable.ic_netology_original_48dp)
            author.text = post.author
            published.text = post.published
            content.text = post.content
            like.text = CountFormat.format(post.likes)
            share.text = CountFormat.format(post.shares)
            seen.text = CountFormat.format(post.views)
            like.isChecked = post.likedByMe
            like.setOnClickListener {
                onInteractionListener.like(post)
            }
            share.setOnClickListener {
                onInteractionListener.share(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
            if (!post.video.isNullOrBlank()) {
                video.visibility = View.VISIBLE
                play.visibility = View.VISIBLE
                video.setImageResource(R.drawable.preview_video)

                val videoClickListener = View.OnClickListener {
                    onInteractionListener.playVideo(post.video!!)
                }

                video.setOnClickListener(videoClickListener)
                play.setOnClickListener(videoClickListener)
            } else {
                video.visibility = View.GONE
                play.visibility = View.GONE
            }

            root.setOnClickListener {
                onInteractionListener.onPostClick(post)
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
        return oldItem == newItem && oldItem.video == newItem.video
    }
}
package com.example.ansteducation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ansteducation.CountFormat
import com.example.ansteducation.R
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.repository.PostRepositoryImpl
import kotlin.concurrent.thread


interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun playVideo(url: String)
    fun onPostClick(post: Post) {}
}

typealias onItemViewListener = (post: Post) -> Unit

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
    private var imgNames: List<String>,
    private val onItemViewListener: onItemViewListener? = null,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    @SuppressLint("NotifyDataSetChanged")
    fun updateImgNames(newImgNames: List<String>) {
        imgNames = newImgNames
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, onItemViewListener, imgNames)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post, position)
        holder.viewed(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val onItemViewListener: onItemViewListener?,
    private val imgNames: List<String>
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("UseKtx")
    fun bind(post: Post, position: Int) {
        binding.apply {
            if (imgNames.isNotEmpty()) {
                val imgIndex = position % imgNames.size
                val imgName = imgNames[imgIndex]
                val imgEndpoint = "http://10.0.2.2:9999/avatars/$imgName"
                Glide.with(root)
                    .load(imgEndpoint)
                    .circleCrop()
                    .placeholder(R.drawable.ic_no_photo)
                    .error(R.drawable.ic_no_photo_error)
                    .timeout(10_000)
                    .into(avatar)
            } else {
                avatar.setImageResource(R.drawable.ic_no_photo)
            }
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
package com.example.ansteducation.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.ansteducation.CountFormat
import com.example.ansteducation.R
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post
import javax.sql.DataSource


interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun playVideo(url: String)
    fun onPostClick(post: Post) {}
    fun retryPost(post: Post)
    fun onImageClick(post: Post, imageUrl: String)
}

typealias onItemViewListener = (post: Post) -> Unit

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val onItemViewListener: onItemViewListener? = null,
) :
    ListAdapter<Post, PostViewHolder>(PostViewHolder.PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener, onItemViewListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        Log.d("ADAPTER_DEBUG", "Position: $position, ID: ${post.id}, Failed: ${post.id < 0}")
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val onItemViewListener: onItemViewListener?,
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("UseKtx")
    fun bind(post: Post) {
        val endpointImg = "http://10.0.2.2:9999/avatars/"
        val endpointAttach = "http://10.0.2.2:9999/media/"
        val isNormalPost = post.id > 0 && post.id < 1_000_000_000_000L
        val isSending = post.id > 1_000_000_000_000L
        val isFailed = post.id < 0

        binding.sendingProgress.isVisible = isSending
        binding.errorToSendPostGroup.isVisible = isFailed

        binding.apply {
            attachment.visibility = View.GONE
            Glide.with(root).clear(attachment)
            Glide.with(root)
                .load(endpointImg + post.authorAvatar)
                .placeholder(R.drawable.ic_avatar_placeholder_48)
                .error(R.drawable.ic_avatar_error_48)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)
            author.text = post.author
            published.text = post.published
            content.text = post.content
            like.isEnabled = isNormalPost
            share.isEnabled = isNormalPost
            menu.isEnabled = isNormalPost
            like.text = CountFormat.format(post.likes)
            share.text = CountFormat.format(post.shares)
            seen.text = CountFormat.format(post.views)
            like.isChecked = post.likedByMe
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
            val attachmentUrl = post.attachment?.url
            if (!attachmentUrl.isNullOrEmpty()) {
                Log.d("POST_ADAPTER", "Attachment found: $attachmentUrl")
                Log.d("POST_ADAPTER", "Full URL: $endpointAttach$attachmentUrl")

                if (attachmentUrl.isNotEmpty()) {
                    val imageUrl = endpointAttach + post.attachment.url
                    attachment.visibility = View.VISIBLE

                    Glide.with(root)
                        .load(imageUrl)
                        .timeout(10_000)
                        .into(attachment)

                    attachment.setOnClickListener {
                        onInteractionListener.onImageClick(post, imageUrl)
                    }
                } else {
                    attachment.visibility = View.GONE
                    Glide.with(root).clear(attachment)
                }


                root.setOnClickListener {
                    onInteractionListener.onPostClick(post)
                }
                refreshBotton.setOnClickListener {
                    if (isFailed) {
                        onInteractionListener.retryPost(post)
                    }
                }
                like.setOnClickListener {
                    onInteractionListener.like(post)
                }
                share.setOnClickListener {
                    onInteractionListener.share(post)
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
            return oldItem == newItem && oldItem.attachment == newItem.attachment
        }
    }
}

package com.example.ansteducation.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ansteducation.CountFormat
import com.example.ansteducation.R
import com.example.ansteducation.adapter.PostViewHolder.PostDiffCallback
import com.example.ansteducation.databinding.CardAdBinding
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Ad
import com.example.ansteducation.dto.FeedItem
import com.example.ansteducation.dto.Post
import com.example.ansteducation.dto.likeDisplayCount
import com.example.ansteducation.util.ServerUrls
import com.example.ansteducation.util.formatApiDateTime


interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun onCommentsClick(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun playVideo(url: String)
    fun onPostClick(post: Post) {}
    fun retryPost(post: Post)
    fun onImageClick(post: Post, imageUrl: String)
}

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("Unknown item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            else -> error("Unknown view type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item  = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("Unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        val fallback = ad.fallbackDrawable ?: R.drawable.ad_banner_placeholder
        val remote = ad.image?.trim()?.takeIf { it.isNotEmpty() }

        binding.root.isVisible = true
        binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        val model: Any = remote?.let { ServerUrls.media(it) } ?: fallback
        Glide.with(binding.root)
            .load(model)
            .placeholder(fallback)
            .error(fallback)
            .timeout(15_000)
            .centerCrop()
            .into(binding.image)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("UseKtx")
    fun bind(post: Post) {
        val isNormalPost = post.id > 0 && post.id < 1_000_000_000_000L
        val isSending = post.id > 1_000_000_000_000L
        val isFailed = post.id < 0

        binding.sendingProgress.isVisible = isSending
        binding.errorToSendPostGroup.isVisible = isFailed

        binding.apply {
            attachment.visibility = View.GONE
            Glide.with(root).clear(attachment)
            Glide.with(root)
                .load(ServerUrls.avatar(post.authorAvatar))
                .placeholder(R.drawable.ic_avatar_placeholder_48)
                .error(R.drawable.ic_avatar_error_48)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)
            author.text = post.author
            published.text = formatApiDateTime(post.published)
            content.text = post.content
            like.isEnabled = isNormalPost
            comments.isEnabled = isNormalPost
            share.isEnabled = isNormalPost
            menu.isEnabled = isNormalPost
            like.text = CountFormat.format(post.likeDisplayCount)
            comments.text = CountFormat.format(post.commentCount)
            share.text = CountFormat.format(post.shares)
            seen.text = CountFormat.format(post.views)
            like.isChecked = post.likedByMe

            menu.isVisible = post.ownedByMe
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
                val imageUrl = ServerUrls.media(post.attachment.url)
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
            comments.setOnClickListener {
                onInteractionListener.onCommentsClick(post)
            }
            share.setOnClickListener {
                onInteractionListener.share(post)
            }
        }
    }

    object PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem == newItem
        }
    }
}

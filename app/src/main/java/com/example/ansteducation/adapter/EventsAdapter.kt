package com.example.ansteducation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ansteducation.R
import com.example.ansteducation.databinding.CardEventBinding
import com.example.ansteducation.dto.Event
import com.example.ansteducation.dto.EventType
import com.example.ansteducation.util.ServerUrls
import com.example.ansteducation.util.formatApiDateTime

interface OnEventInteractionListener {
    fun onLike(event: Event) {}
    fun onRemove(event: Event) {}
    fun onEdit(event: Event) {}
    fun onClick(event: Event) {}
}

class EventsAdapter(
    private val onInteractionListener: OnEventInteractionListener,
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnEventInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            author.text = event.author
            authorJob.text = event.authorJob ?: root.context.getString(R.string.author_job_searching)
            published.text = formatApiDateTime(event.published)
            content.text = event.content
            eventType.text = if (event.type == EventType.OFFLINE) {
                root.context.getString(R.string.event_type_offline)
            } else {
                root.context.getString(R.string.event_type_online)
            }
            datetime.text = formatApiDateTime(event.datetime)
            like.isChecked = event.likedByMe
            like.text = event.likes.toString()

            Glide.with(root)
                .load(ServerUrls.avatar(event.authorAvatar))
                .placeholder(R.drawable.ic_avatar_placeholder_48)
                .error(R.drawable.ic_avatar_error_48)
                .circleCrop()
                .into(avatar)

            menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(event)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(event)
            }

            root.setOnClickListener {
                onInteractionListener.onClick(event)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}

package com.example.ansteducation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ansteducation.R
import com.example.ansteducation.databinding.ItemEventBinding
import com.example.ansteducation.dto.Event

interface OnEventInteractionListener {
    fun like(event: Event)
    fun remove(event: Event)
    fun onClick(event: Event)
}

class EventsAdapter(
    private val listener: OnEventInteractionListener
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    var items: List<Event> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class EventViewHolder(
        private val binding: ItemEventBinding,
        private val listener: OnEventInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            val endpointImg = "http://10.0.2.2:9999/avatars/"

            binding.author.text = event.author
            binding.published.text = event.published
            binding.datetime.text = event.datetime
            binding.type.text = if (event.type.name == "OFFLINE") {
                binding.root.context.getString(R.string.event_offline)
            } else {
                binding.root.context.getString(R.string.event_online)
            }
            binding.content.text = event.content
            binding.like.text = event.likes.toString()
            binding.like.isChecked = event.likedByMe

            Glide.with(binding.root)
                .load(event.authorAvatar?.let { endpointImg + it })
                .placeholder(R.drawable.ic_avatar_placeholder_48)
                .error(R.drawable.ic_avatar_error_48)
                .circleCrop()
                .into(binding.avatar)

            val hasLink = !event.link.isNullOrBlank()
            binding.link.visibility = if (hasLink) View.VISIBLE else View.GONE
            if (hasLink) {
                binding.link.text = event.link
                binding.link.setOnClickListener {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        event.link!!.toUri()
                    )
                    binding.root.context.startActivity(intent)
                }
            }

            binding.like.setOnClickListener {
                listener.like(event)
            }

            binding.menu.setOnClickListener {
                if (event.ownedByMe) {
                    listener.remove(event)
                }
            }

            binding.root.setOnClickListener {
                listener.onClick(event)
            }
        }
    }
}


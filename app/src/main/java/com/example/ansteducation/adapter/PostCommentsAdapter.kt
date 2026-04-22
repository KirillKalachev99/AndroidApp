package com.example.ansteducation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ansteducation.databinding.ItemPostCommentBinding
import com.example.ansteducation.dto.PostComment
import com.example.ansteducation.util.formatApiDateTime

class PostCommentsAdapter : ListAdapter<PostComment, PostCommentsAdapter.Vh>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemPostCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(getItem(position))
    }

    class Vh(private val binding: ItemPostCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(c: PostComment) {
            binding.author.text = c.author
            binding.published.text = formatApiDateTime(c.published)
            binding.content.text = c.content
        }
    }

    private object Diff : DiffUtil.ItemCallback<PostComment>() {
        override fun areItemsTheSame(a: PostComment, b: PostComment): Boolean = a.id == b.id
        override fun areContentsTheSame(a: PostComment, b: PostComment): Boolean = a == b
    }
}

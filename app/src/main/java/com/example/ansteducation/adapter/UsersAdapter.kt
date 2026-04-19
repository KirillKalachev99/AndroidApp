package com.example.ansteducation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ansteducation.R
import com.example.ansteducation.util.ServerUrls
import com.example.ansteducation.databinding.ItemUserBinding
import com.example.ansteducation.dto.User

class UsersAdapter(
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    var items: List<User> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.userName.text = user.name
            binding.userLogin.text = user.login

            Glide.with(binding.root)
                .load(ServerUrls.avatar(user.avatar))
                .placeholder(R.drawable.ic_avatar_placeholder_48)
                .error(R.drawable.ic_avatar_error_48)
                .circleCrop()
                .into(binding.userAvatar)

            binding.root.setOnClickListener {
                onClick(user)
            }
        }
    }
}


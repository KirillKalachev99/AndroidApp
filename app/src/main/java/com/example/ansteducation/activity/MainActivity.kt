package com.example.ansteducation.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.databinding.ActivityMainBinding
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post
import com.example.ansteducation.util.AndroidUtils
import com.example.ansteducation.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val cardPostBinding = CardPostBinding.inflate(layoutInflater, binding.root, false)

        setContentView(binding.root)

        cardPostBinding.avatar.setImageResource(R.drawable.ic_netology_original_48dp)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val horizontalPadding =
                systemBars.left + resources.getDimensionPixelSize(R.dimen.common_margin)
            val topPadding = systemBars.top + resources.getDimensionPixelSize(R.dimen.common_margin)
            val bottomPadding =
                systemBars.bottom + resources.getDimensionPixelSize(R.dimen.common_margin)

            v.setPadding(
                horizontalPadding,
                topPadding,
                horizontalPadding,
                bottomPadding
            )
            insets
        }

        val viewModel: PostViewModel by viewModels()

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) {
                viewModel.like(post.id)
            }

            override fun share(post: Post) {
                viewModel.repost(post.id)
            }

            override fun remove(post: Post) {
                viewModel.remove(post.id)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
            }
        }) {
            viewModel.view(it.id)
        }

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            val new = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (new) binding.list.smoothScrollToPosition(0)
            }
        }

        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                with(binding.content) {
                    setText(post.content)
                    AndroidUtils.showKeyboard(this)
                }
                binding.apply {
                    postTextPreview.text = post.content
                    editGroup.visibility = View.VISIBLE
                    editBorder.visibility = View.VISIBLE
                }
            }
        }

        binding.apply {
            closeEdit.setOnClickListener {
                editBorder.visibility = View.GONE
                editGroup.visibility = View.GONE
                content.setText("")
                content.clearFocus()
                AndroidUtils.hideKeyboard(content)
                viewModel.clear()
            }
            save.setOnClickListener {
                val text = binding.content.text.toString()
                if (text.isBlank()) {
                    Toast.makeText(this@MainActivity, R.string.error_empty_content, Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                viewModel.save(text)
                content.setText("")
                content.clearFocus()
                editGroup.visibility = View.GONE
                editBorder.visibility = View.GONE
                AndroidUtils.hideKeyboard(content)
                viewModel.clear()
            }
        }
    }
}


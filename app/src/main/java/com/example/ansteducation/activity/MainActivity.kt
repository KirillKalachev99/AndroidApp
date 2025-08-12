package com.example.ansteducation.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.R
import com.example.ansteducation.adapter.OnInteractionListener
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.databinding.ActivityMainBinding
import com.example.ansteducation.databinding.CardPostBinding
import com.example.ansteducation.dto.Post
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
        val newPostLauncher = registerForActivityResult(NewPostContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.save(result)
        }
        val editPostLauncher = registerForActivityResult(EditPostContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.save(result)
        }

        val adapter = PostAdapter(object : OnInteractionListener {
            override fun like(post: Post) {
                viewModel.like(post.id)
            }

            override fun share(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
                viewModel.repost(post.id)
            }

            override fun remove(post: Post) {
                viewModel.remove(post.id)
            }

            override fun edit(post: Post) {
                editPostLauncher.launch(post.content)
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

        binding.add.setOnClickListener {
            newPostLauncher.launch()
        }
    }
}


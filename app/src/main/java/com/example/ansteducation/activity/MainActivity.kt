package com.example.ansteducation.activity

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.R
import com.example.ansteducation.adapter.PostAdapter
import com.example.ansteducation.databinding.ActivityMainBinding
import com.example.ansteducation.databinding.CardPostBinding
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

        val adapter = PostAdapter({
            viewModel.like(it.id)
        }, {
            viewModel.repost(it.id)
        }, {
            viewModel.view(it.id)
        })

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }
    }
}


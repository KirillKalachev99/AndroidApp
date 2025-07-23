package com.example.ansteducation.activity

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.CountFormat
import com.example.ansteducation.R
import com.example.ansteducation.databinding.ActivityMainBinding
import com.example.ansteducation.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.avatar.setImageResource(R.drawable.ic_netology_original_48dp)

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

        if (savedInstanceState == null) {
            viewModel.view()
        }

        viewModel.data.observe(this) { post ->
            with(binding) {
                avatar.setImageResource(R.drawable.ic_netology_original_48dp)
                author.text = post.author
                published.text = post.published
                content.text = post.content
                likeCount.text = CountFormat.format(post.likes)
                share.setImageResource(R.drawable.ic_share_24)
                shareCount.text = CountFormat.format(post.shares)
                seen.setImageResource(R.drawable.ic_eye_24)
                seenCount.text = CountFormat.format(post.views)
                if (post.liked) {
                    like.setImageResource(R.drawable.ic_liked_24)
                } else {
                    like.setImageResource(R.drawable.ic_like_24)
                }
            }
        }

        binding.like.setOnClickListener {
            viewModel.like()
        }

        binding.share.setOnClickListener {
            viewModel.repost()
        }
    }
}

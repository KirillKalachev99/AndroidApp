package com.example.ansteducation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ansteducation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val post = Post(
            id = "1",
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 1234,
            shares = 1999,
            views = 12344
        )
        setContentView(binding.root)
        with(binding){
            avatar.setImageResource(R.drawable.ic_netology_original_48dp)
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = CountFormat.format(post.likes)
            share.setImageResource(R.drawable.ic_share_24)
            shareCount.text = CountFormat.format(post.shares)
            seen.setImageResource(R.drawable.ic_eye_24)
            post.views++
            seenCount.text = CountFormat.format(post.views)

            if (post.liked){
                like.setImageResource(R.drawable.ic_liked_24)
            }

            like.setOnClickListener {
                post.liked = !post.liked
                like.setImageResource(
                    if(post.liked) R.drawable.ic_liked_24 else R.drawable.ic_like_24
                )
                if (post.liked) post.likes++ else post.likes--
                likeCount.text = CountFormat.format(post.likes)
            }

            share.setOnClickListener{
                if(!post.sharedByMe) {
                    post.shares++
                    post.sharedByMe = true
                }
                shareCount.text = CountFormat.format(post.shares)
            }
        }
    }
}
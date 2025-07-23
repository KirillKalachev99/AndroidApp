package com.example.ansteducation.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.dto.Post

class PostRepositoryInMemory: PostRepository {

    private var post = Post(
        id = "1",
        author = "Нетология. Университет интернет-профессий будущего",
        published = "21 мая в 18:36",
        content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
        likes = 1234,
        shares = 1999,
        views = 999
    )

    private val _data = MutableLiveData(post)

    override fun get(): LiveData<Post> = _data

    override fun like() {

        post = post.copy(
            liked = !post.liked,
            likes = if (post.liked) {
                post.likes - 1
            } else {
                post.likes + 1
            }
        )
        _data.value = post
    }

    override fun repost() {
        post = post.copy(
            sharedByMe = true,
            shares = post.shares + 1
        )
        _data.value = post
    }

    override fun view() {
        post = post.copy(
            views = post.views + 1
        )
        _data.value = post
    }
}
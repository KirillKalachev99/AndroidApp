package com.example.ansteducation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.db.AppDb
import com.example.ansteducation.dto.Post
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = ""
)


class PostViewModel(application: Application) : AndroidViewModel(application) {

    val postWithVideo = Post(
        id = 5,
        author = "Me",
        published = "Now",
        content = "Описание поста с видео",
        video = "https://rutube.ru/video/c6cc4d620b1d4338901770a44b3e82f4/"
    )

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<List<Post>> = repository.get()
    val edited = MutableLiveData(empty)

    fun like(id: Long) = repository.likeById(id)
    fun repost(id: Long) = repository.shareById(id)
    fun view(id: Long) = repository.viewById(id)
    fun remove(id: Long) = repository.removeById(id)

    fun save(text: String) {
        edited.value?.let {
            val content = text.trim()
            if (it.id == 0L) {
                repository.save(empty.copy(content = content))
            } else {
                repository.save(it.copy(content = content))
            }
        }
        clear()
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clear(){
        edited.value = empty
    }

    fun addVideoPost(post: Post){
        repository.addVideoPost(post)
    }

}
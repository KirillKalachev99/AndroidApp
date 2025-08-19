package com.example.ansteducation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ansteducation.dto.Post
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryFileImpl


private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryFileImpl(application)

    val data: LiveData<List<Post>> = repository.get()
    val edited = MutableLiveData(empty)

    fun like(id: Long) = repository.likeById(id)
    fun repost(id: Long) = repository.repostById(id)
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
}
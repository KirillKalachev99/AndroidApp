package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.ansteducation.dto.Post
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryInMemory

class PostViewModel : ViewModel() {

    private val repository: PostRepository = PostRepositoryInMemory()

    val data: LiveData<List<Post>> = repository.get()
    fun like(id: Long) = repository.likeById(id)
    fun repost(id: Long) = repository.repostById(id)
    fun view(id: Long) = repository.viewById(id)
}
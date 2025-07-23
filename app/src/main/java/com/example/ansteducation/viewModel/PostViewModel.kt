package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.ansteducation.dto.Post
import com.example.ansteducation.repository.PostRepository
import com.example.ansteducation.repository.PostRepositoryInMemory

class PostViewModel : ViewModel() {

    private val repository: PostRepository = PostRepositoryInMemory()
    val data: LiveData<Post> = repository.get()
    fun like() = repository.like()
    fun repost() = repository.repost()
    fun view() = repository.view()
}
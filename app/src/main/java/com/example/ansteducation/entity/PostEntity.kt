package com.example.ansteducation.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ansteducation.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val sharedByMe: Boolean = false,
    val likedByMe: Boolean = false,
    var viewedByMe: Boolean = false,
    var video: String? = null,
    val syncStatus: String = "SYNCED"
) {
    fun toDto(): Post = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likes = likes,
        shares = shares,
        views = views,
        sharedByMe = sharedByMe,
        likedByMe = likedByMe,
        viewedByMe = viewedByMe,
        video = video
    )

    companion object {
        fun fromDto(post: Post): PostEntity = with(post) {
            PostEntity(
                id = id,
                author = author,
                published = published,
                content = content,
                likes = likes,
                shares = shares,
                views = views,
                sharedByMe = sharedByMe,
                likedByMe = likedByMe,
                viewedByMe = viewedByMe,
                video = video,
                syncStatus = "SYNCED"
            )
        }
    }
}


fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    author = author,
    published = published,
    content = content,
    likes = likes,
    shares = shares,
    views = views,
    sharedByMe = sharedByMe,
    likedByMe = likedByMe,
    viewedByMe = viewedByMe,
    video = video
)
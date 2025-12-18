package com.example.ansteducation.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ansteducation.dto.Attachment
import com.example.ansteducation.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorId: Long,
    val published: String,
    val content: String,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val sharedByMe: Boolean = false,
    val likedByMe: Boolean = false,
    var viewedByMe: Boolean = false,
    var video: String? = null,
    val syncStatus: String = "SYNCED",
    @Embedded
    val attachment: Attachment? = null,
) {
    fun toDto(): Post = Post(
        id = id,
        author = author,
        authorAvatar = authorAvatar,
        authorId = authorId,
        published = published,
        content = content,
        likes = likes,
        shares = shares,
        views = views,
        sharedByMe = sharedByMe,
        likedByMe = likedByMe,
        viewedByMe = viewedByMe,
        video = video,
        attachment = attachment
    )

    companion object {
        fun fromDto(post: Post): PostEntity = with(post) {
            PostEntity(
                id = id,
                author = author,
                authorAvatar = authorAvatar,
                authorId = authorId,
                published = published,
                content = content,
                likes = likes,
                shares = shares,
                views = views,
                sharedByMe = sharedByMe,
                likedByMe = likedByMe,
                viewedByMe = viewedByMe,
                video = video,
                syncStatus = "SYNCED",
                attachment = attachment
            )
        }
    }
}


fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    author = author,
    authorId = authorId,
    published = published,
    content = content,
    likes = likes,
    shares = shares,
    views = views,
    sharedByMe = sharedByMe,
    likedByMe = likedByMe,
    viewedByMe = viewedByMe,
    video = video,
    attachment = attachment
)
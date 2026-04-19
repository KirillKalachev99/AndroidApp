package com.example.ansteducation.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ansteducation.dto.Attachment
import com.example.ansteducation.dto.Coordinates
import com.example.ansteducation.dto.Post
import com.example.ansteducation.dto.likeDisplayCount

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
    val commentCount: Int = 0,
    val views: Int = 0,
    val sharedByMe: Boolean = false,
    val likedByMe: Boolean = false,
    var viewedByMe: Boolean = false,
    var video: String? = null,
    val syncStatus: String = "SYNCED",
    @Embedded
    val attachment: Attachment? = null,
    val link: String? = null,
    val authorJob: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
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
        commentCount = commentCount,
        views = views,
        sharedByMe = sharedByMe,
        likedByMe = likedByMe,
        viewedByMe = viewedByMe,
        video = video,
        attachment = attachment,
        link = link,
        coords = if (lat != null && lon != null) Coordinates(lat, lon) else null,
        mentions = emptyList(),
        authorJob = authorJob,
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
                likes = post.likeDisplayCount,
                shares = shares,
                commentCount = commentCount,
                views = views,
                sharedByMe = sharedByMe,
                likedByMe = likedByMe,
                viewedByMe = viewedByMe,
                video = video,
                syncStatus = "SYNCED",
                attachment = attachment,
                link = link,
                authorJob = authorJob,
                lat = coords?.lat,
                lon = coords?.lon,
            )
        }
    }
}


fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    author = author,
    authorAvatar = authorAvatar,
    authorId = authorId,
    published = published,
    content = content,
    likes = likeDisplayCount,
    shares = shares,
    commentCount = commentCount,
    views = views,
    sharedByMe = sharedByMe,
    likedByMe = likedByMe,
    viewedByMe = viewedByMe,
    video = video,
    attachment = attachment,
    link = link,
    authorJob = authorJob,
    lat = coords?.lat,
    lon = coords?.lon,
)
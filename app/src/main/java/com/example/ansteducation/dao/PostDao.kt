package com.example.ansteducation.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ansteducation.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY ABS(id) DESC, id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)


    suspend fun save(post: PostEntity) = if (post.id == 0L) {
        insert(post)
    } else {
        updateContentById(post.id, post.content)
    }

    @Query(
        """
           UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
           UPDATE PostEntity SET
               views = views + 1,
               viewedByMe = CASE WHEN viewedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
        """
    )
    suspend fun viewById(id: Long)

    @Query(
        """
           UPDATE PostEntity SET
               shares = shares + 1,
               sharedByMe = sharedByMe + 1
           WHERE id = :id;
        """
    )
    suspend fun shareById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT COUNT(*) FROM PostEntity WHERE id = :id")
    suspend fun exists(id: Long): Int

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun getCount(): Int
}
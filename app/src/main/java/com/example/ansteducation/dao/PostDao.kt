package com.example.ansteducation.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ansteducation.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY ABS(id) DESC, id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY ABS(id) DESC, id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

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

    @Query("""
        UPDATE PostEntity SET 
            likedByMe = CASE WHEN likedByMe = 1 THEN 0 ELSE 1 END,
            likes = CASE WHEN likedByMe = 1 THEN likes - 1 ELSE likes + 1 END
        WHERE id = :id
    """)
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

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}
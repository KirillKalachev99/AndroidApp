package com.example.ansteducation.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.ansteducation.dto.Post

class PostDaoImpl(private val db: SQLiteDatabase) : PostDao {
    companion object {
        val DDL = """
        CREATE TABLE ${PostColumns.TABLE} (
            ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
            ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
            ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL,
            ${PostColumns.COLUMN_LIKED_BY_ME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_SHARED_BY_ME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_VIEWED_BY_ME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_SHARES} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_VIEWS} INTEGER NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_VIDEO} TEXT,
        );
        """.trimIndent()
    }

    object PostColumns {
        const val TABLE = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_LIKED_BY_ME = "likedByMe"
        const val COLUMN_SHARED_BY_ME = "sharedByMe"
        const val COLUMN_VIEWED_BY_ME = "viewedByMe"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_SHARES = "shares"
        const val COLUMN_VIEWS = "views"
        const val COLUMN_VIDEO = "video"

        val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_AUTHOR,
            COLUMN_PUBLISHED,
            COLUMN_CONTENT,
            COLUMN_LIKES,
            COLUMN_VIEWS,
            COLUMN_SHARES,
            COLUMN_VIDEO,
            COLUMN_LIKED_BY_ME,
            COLUMN_SHARED_BY_ME,
            COLUMN_VIEWED_BY_ME,
        )
    }

    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            put(PostColumns.COLUMN_AUTHOR, "Me")
            put(PostColumns.COLUMN_CONTENT, post.content)
            put(PostColumns.COLUMN_PUBLISHED, "now")
        }
        val id = if (post.id != 0L) {
            db.update(
                PostColumns.TABLE,
                values,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(post.id.toString()),
            )
            post.id
        } else {
            db.insert(PostColumns.TABLE, null, values)
        }
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likeById(id: Long) {
        db.execSQL(
            """
           UPDATE posts SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = ?;
        """.trimIndent(), arrayOf(id)
        )
    }

    override fun removeById(id: Long) {
        db.delete(
            PostColumns.TABLE,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                id = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                likes = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                shares = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARES)),
                views = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_VIEWS)),
                likedByMe = getBoolean(getColumnIndexOrThrow(PostColumns.COLUMN_LIKED_BY_ME)),
                sharedByMe = getBoolean(getColumnIndexOrThrow(PostColumns.COLUMN_SHARED_BY_ME)),
                viewedByMe = getBoolean(getColumnIndexOrThrow(PostColumns.COLUMN_VIEWED_BY_ME)),
                video = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEO))
            )
        }
    }

    private fun Cursor.getBoolean(columnIndex: Int): Boolean {
        return getInt(columnIndex) != 0
    }

    override fun viewById(id: Long) {
        db.execSQL(
            """
           UPDATE posts SET
               views = views + 1,
               viewedByMe = CASE WHEN viewedByMe THEN 0 ELSE 1 END
           WHERE id = ?;
        """.trimIndent(), arrayOf(id)
        )
    }

    override fun shareById(id: Long) {
        db.execSQL(
            """
           UPDATE posts SET
               shares = shares + 1,
               sharedByMe = sharedByMe + 1
           WHERE id = ?;
        """.trimIndent(), arrayOf(id)
        )
    }

    override fun addPostWithVideo() {
        val videoUrl = "https://rutube.ru/video/c6cc4d620b1d4338901770a44b3e82f4/"
        val query = "SELECT id FROM posts WHERE video = '$videoUrl'"
        db.rawQuery(query, null).use { cursor ->
            if (cursor.count == 0) {
                val values = ContentValues().apply {
                    put(PostColumns.COLUMN_AUTHOR, "Me")
                    put(PostColumns.COLUMN_CONTENT, "Описание поста с видео")
                    put(PostColumns.COLUMN_PUBLISHED, "now")
                    put(PostColumns.COLUMN_LIKED_BY_ME, 0)
                    put(PostColumns.COLUMN_SHARED_BY_ME, 0)
                    put(PostColumns.COLUMN_VIEWED_BY_ME, 0)
                    put(PostColumns.COLUMN_LIKES, 0)
                    put(PostColumns.COLUMN_SHARES, 0)
                    put(PostColumns.COLUMN_VIEWS, 0)
                    put(PostColumns.COLUMN_VIDEO, videoUrl)
                }
                db.insert(PostColumns.TABLE, null, values)
            }
        }
    }
}

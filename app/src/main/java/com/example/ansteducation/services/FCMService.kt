package com.example.ansteducation.services

import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ansteducation.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import android.app.Notification
import android.os.Build
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import androidx.core.app.NotificationManagerCompat
import com.example.ansteducation.activity.AppActivity
import com.example.ansteducation.auth.AppAuth
import com.example.ansteducation.dto.PushBody
import kotlin.jvm.java
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val gson = Gson()
    private val channelId = "remote"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i("fcm msg", message.data.toString())
        val actionFromData = message.data[action]
        val appAuthUserId = AppAuth.getInstance().authState.value?.id

        message.data[content]?.let { content ->
            val pushBody = gson.fromJson(content, PushBody::class.java)
            Log.i("fcm msg", pushBody.toString())
            when (pushBody.recipientId) {
                appAuthUserId -> handlePushFromServer(pushBody)
                null -> handlePushFromServer(pushBody)
                0L -> AppAuth.getInstance().sendPushToken()
                else -> AppAuth.getInstance().sendPushToken()
            }
        }

        message.data[action]?.let { action ->
            Log.i("fcm msg", actionFromData.toString())
            when (Action.valueOf(action)) {
                Action.LIKE -> handleLike(
                    gson.fromJson(message.data[content], Like::class.java)
                )

                Action.POST -> handlePost(
                    gson.fromJson(message.data[content], NewPost::class.java)
                )
            }
        }
    }

    private fun isActionInEnum(action: String): Boolean {
        return enumValues<Action>().any { it.name == action }
    }

    private fun handleLike(like: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    like.userName,
                    like.postAuthor
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notify(notification)
    }

    private fun handlePushFromServer(pushBody: PushBody) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText(pushBody.content)
            .build()
        notify(notification)
    }

    private fun handlePost(post: NewPost) {

        val intent = Intent(this, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val fullText = post.postText
        val shortText = if (fullText.length > 100) {
            fullText.take(100) + "…"
        } else {
            fullText
        }
        val icon = Icon.createWithResource(this, R.drawable.ic_notification)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_new_post,
                    post.userName
                )
            )
            .setContentText(
                getString(
                    R.string.notification_new_post_text,
                    shortText
                )
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fullText)
            )
            .setLargeIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()
        notify(notification)
    }

    private fun notify(notification: Notification) {
        val isUpperTiramisu = Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
        val isPostNotificationGranted = if (isUpperTiramisu) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        if (isPostNotificationGranted) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    enum class Action {
        LIKE, POST,
    }

    data class Like(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postAuthor: String
    )

    data class NewPost(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postText: String
    )
}
package com.example.ansteducation.auth

import android.content.Context
import androidx.core.content.edit
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dto.PushToken
import com.example.ansteducation.dto.Token
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val firebaseMessaging: FirebaseMessaging
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val ID_KEY = "id"
    private val TOKEN_KEY = "token"
    private val _authState = MutableStateFlow<Token?>(null)
    val authState = _authState.asStateFlow()

    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            prefs.edit { clear() }
        } else {
            _authState.value = Token(id, token)
        }

        sendPushToken()
    }

    @Synchronized
    fun setAuth(token: Token) {
        _authState.value = token
        prefs.edit {
            putLong(ID_KEY, token.id)
            putString(TOKEN_KEY, token.token)
        }
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getPostApiService(): PostApi
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context,
                    AppAuthEntryPoint::class.java
                ).getPostApiService()
                entryPoint.sendPushToken(
                    PushToken(
                        token ?: firebaseMessaging.token.await()
                    )
                )
            }
                .onFailure { it.printStackTrace() }
        }
    }

    @Synchronized
    fun clear() {
        _authState.value = null
        prefs.edit { clear() }
    }
}
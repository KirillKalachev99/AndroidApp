package com.example.ansteducation.auth

import android.content.Context
import androidx.core.content.edit
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dto.PushToken
import com.example.ansteducation.dto.Token
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.EmptyCoroutineContext


class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow<Token?>(null)
    val authState = _authState.asStateFlow()

    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            prefs.edit() { clear() }
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

    fun sendPushToken(token: String? = null) {
        CoroutineScope(EmptyCoroutineContext).launch {
            runCatching {
                PostApi.service.sendPushToken(
                    PushToken(
                        token ?: FirebaseMessaging.getInstance().token.await()
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

    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"

        @Volatile
        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE) { "Should call initApp first!" }

        fun initApp(context: Context) {
            INSTANCE = AppAuth(context)
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)

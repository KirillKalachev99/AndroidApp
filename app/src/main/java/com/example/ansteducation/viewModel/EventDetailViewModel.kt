package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.EventApi
import com.example.ansteducation.api.UserApi
import com.example.ansteducation.dto.Event
import com.example.ansteducation.dto.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventApi: EventApi,
    private val userApi: UserApi,
) : ViewModel() {

    private val _event = MutableLiveData<Event?>(null)
    val event: LiveData<Event?> = _event

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun load(eventId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val eventAsync = async { eventApi.getById(eventId) }
                val usersAsync = async { userApi.getAll() }
                _event.value = eventAsync.await()
                _users.value = usersAsync.await()
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить событие"
            } finally {
                _loading.value = false
            }
        }
    }

    fun namesForIds(ids: List<Long>, all: List<User>): String {
        if (ids.isEmpty()) return "—"
        val byId = all.associateBy { it.id }
        return ids.joinToString("\n") { id ->
            val u = byId[id]
            if (u != null) "${u.name} (@${u.login})" else "id: $id"
        }
    }
}

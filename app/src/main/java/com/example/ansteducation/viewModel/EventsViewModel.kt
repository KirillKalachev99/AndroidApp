package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.EventApi
import com.example.ansteducation.dto.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventApi: EventApi
) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadEvents() {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _events.value = eventApi.getAll()
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить события"
            } finally {
                _loading.value = false
            }
        }
    }

    fun like(event: Event) {
        viewModelScope.launch {
            try {
                val updated = if (!event.likedByMe) {
                    eventApi.likeById(event.id)
                } else {
                    eventApi.dislikeById(event.id)
                }
                _events.value = _events.value?.map { if (it.id == updated.id) updated else it }
            } catch (_: Exception) {
            }
        }
    }

    fun remove(event: Event) {
        viewModelScope.launch {
            try {
                eventApi.deleteById(event.id)
                _events.value = _events.value?.filterNot { it.id == event.id }
            } catch (_: Exception) {
            }
        }
    }
}


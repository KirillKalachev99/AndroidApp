package com.example.ansteducation.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.R
import com.example.ansteducation.api.EventApi
import com.example.ansteducation.api.PostApi
import com.example.ansteducation.dto.Attachment
import com.example.ansteducation.dto.AttachmentType
import com.example.ansteducation.dto.Event
import com.example.ansteducation.util.requiresSignIn
import com.example.ansteducation.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventApi: EventApi,
    private val postApi: PostApi,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _saveFinished = SingleLiveEvent<Unit>()
    val saveFinished: LiveData<Unit> = _saveFinished

    private val _snackbarMessage = SingleLiveEvent<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private fun postSnackbarFromException(e: Exception, fallbackRes: Int) {
        _snackbarMessage.value = when {
            e.requiresSignIn() -> appContext.getString(R.string.snackbar_sign_in_required)
            else -> e.message?.takeIf { it.isNotBlank() }
                ?: appContext.getString(fallbackRes)
        }
    }

    fun loadEvents() {
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

    suspend fun fetchEvent(id: Long): Event = eventApi.getById(id)

    fun saveEvent(event: Event, imageFile: File?) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                var toSend = event
                if (imageFile != null) {
                    val size = imageFile.length()
                    if (size > MAX_ATTACHMENT_BYTES) {
                        _snackbarMessage.value = appContext.getString(R.string.attachment_too_large)
                        _loading.value = false
                        return@launch
                    }
                    val media = postApi.upload(
                        MultipartBody.Part.createFormData(
                            "file",
                            imageFile.name,
                            imageFile.asRequestBody()
                        )
                    )
                    toSend = toSend.copy(
                        attachment = Attachment(
                            url = media.id,
                            type = AttachmentType.IMAGE
                        )
                    )
                }
                eventApi.save(toSend)
                loadEvents()
                _saveFinished.value = Unit
            } catch (e: Exception) {
                postSnackbarFromException(e, R.string.error_title)
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
            } catch (e: Exception) {
                postSnackbarFromException(e, R.string.no_response_like)
            }
        }
    }

    fun remove(event: Event) {
        viewModelScope.launch {
            try {
                eventApi.deleteById(event.id)
                _events.value = _events.value?.filterNot { it.id == event.id }
            } catch (e: Exception) {
                postSnackbarFromException(e, R.string.error_title)
            }
        }
    }

    companion object {
        private const val MAX_ATTACHMENT_BYTES = 15L * 1024 * 1024
    }
}

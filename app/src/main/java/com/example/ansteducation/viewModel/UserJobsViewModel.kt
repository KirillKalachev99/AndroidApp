package com.example.ansteducation.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.R
import com.example.ansteducation.api.JobApi
import com.example.ansteducation.dto.Job
import com.example.ansteducation.util.SingleLiveEvent
import com.example.ansteducation.util.requiresSignIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class UserJobsViewModel @Inject constructor(
    private val jobApi: JobApi,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _jobs = MutableLiveData<List<Job>>(emptyList())
    val jobs: LiveData<List<Job>> = _jobs

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _snackbarMessage = SingleLiveEvent<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    fun loadUserJobs(userId: Long) {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _jobs.value = loadJobsForUser(userId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить список работ"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMyJobs() {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _jobs.value = jobApi.getMyJobs()
            } catch (e: Exception) {
                _error.value = e.message ?: "Не удалось загрузить список работ"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addJob(name: String, position: String, start: String, finish: String?) {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val job = Job(
                    id = 0,
                    name = name,
                    position = position,
                    start = start,
                    finish = finish,
                )
                jobApi.save(job)
                _jobs.value = jobApi.getMyJobs()
            } catch (e: Exception) {
                if (e.requiresSignIn()) {
                    _snackbarMessage.value = appContext.getString(R.string.snackbar_sign_in_required)
                    _error.value = null
                } else {
                    _error.value = e.message ?: "Не удалось сохранить работу"
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteJob(id: Long) {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                jobApi.deleteById(id)
                _jobs.value = jobApi.getMyJobs()
            } catch (e: Exception) {
                if (e.requiresSignIn()) {
                    _snackbarMessage.value = appContext.getString(R.string.snackbar_sign_in_required)
                    _error.value = null
                } else {
                    _error.value = e.message ?: "Не удалось удалить работу"
                }
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun loadJobsForUser(userId: Long): List<Job> =
        try {
            jobApi.getUserJobsByAuthor(userId)
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
            try {
                jobApi.getUserJobsByUserId(userId)
            } catch (e2: HttpException) {
                if (e2.code() != 404) throw e2
                try {
                    jobApi.getUserJobsByPath(userId)
                } catch (e3: HttpException) {
                    if (e3.code() == 404) emptyList()
                    else throw e3
                }
            }
        }
}


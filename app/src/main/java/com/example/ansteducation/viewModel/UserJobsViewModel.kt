package com.example.ansteducation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ansteducation.api.JobApi
import com.example.ansteducation.dto.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserJobsViewModel @Inject constructor(
    private val jobApi: JobApi
) : ViewModel() {

    private val _jobs = MutableLiveData<List<Job>>(emptyList())
    val jobs: LiveData<List<Job>> = _jobs

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadUserJobs(userId: Long) {
        if (_loading.value == true) return
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _jobs.value = jobApi.getUserJobs(userId)
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
                _error.value = e.message ?: "Не удалось сохранить работу"
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
                _error.value = e.message ?: "Не удалось удалить работу"
            } finally {
                _loading.value = false
            }
        }
    }
}


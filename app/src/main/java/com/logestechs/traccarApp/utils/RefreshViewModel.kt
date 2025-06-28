package com.logestechs.traccarApp.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RefreshViewModel : ViewModel() {

    private val _dataRefresh = MutableLiveData<Boolean>()
    val dataRefresh: LiveData<Boolean>
        get() = _dataRefresh

    fun refreshData() {
        _dataRefresh.value = true
    }

    fun onDataRefreshed() {
        _dataRefresh.value = false
    }
}
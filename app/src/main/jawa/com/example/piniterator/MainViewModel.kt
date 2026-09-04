kotlin
package com.example.piniterator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = PinStorage(application)
    
    private val _pin = MutableLiveData<String?>("Натисніть Далі")
    val pin: LiveData<String?> = _pin
    
    private val _progress = MutableLiveData("0/10000")
    val progress: LiveData<String> = _progress
    
    private val _finished = MutableLiveData(false)
    val finished: LiveData<Boolean> = _finished

    fun onNext() {
        if (_finished.value == true) return
        val number = storage.getNext()
        if (number != null) {
            _pin.value = String.format("%04d", number)
            _progress.value = "${storage.count}/10000"
        } else {
            _pin.value = "Всі перебрано"
            _finished.value = true
        }
    }

    fun onReset() {
        storage.reset()
        _pin.value = "Натисніть Далі"
        _progress.value = "0/10000"
        _finished.value = false
    }
}

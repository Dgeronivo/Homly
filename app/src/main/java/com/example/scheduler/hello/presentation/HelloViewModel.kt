package com.example.scheduler.hello.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelloViewModel : ViewModel() {
    private val _nameInput = MutableStateFlow("")
    val nameInput: StateFlow<String> = _nameInput.asStateFlow()

    private val _submittedName = MutableStateFlow<String?>(null)
    val submittedName: StateFlow<String?> = _submittedName.asStateFlow()

    fun onNameChange(name: String) {
        _nameInput.value = name
    }

    fun onSubmit() {
        _submittedName.value = _nameInput.value
    }
}

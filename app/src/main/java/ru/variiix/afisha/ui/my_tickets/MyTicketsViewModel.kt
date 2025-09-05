package ru.variiix.afisha.ui.my_tickets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyTicketsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is my tickets Fragment"
    }
    val text: LiveData<String> = _text
}
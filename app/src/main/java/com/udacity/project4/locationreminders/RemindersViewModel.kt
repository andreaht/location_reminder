package com.udacity.project4.locationreminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.authentication.FirebaseUserLiveData

class RemindersViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}
package com.dicoding.storyapp.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.User
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.remote.ApiService
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class RegisterViewModel(private val pref: UserPreference, private val apiService: ApiService) :
    ViewModel() {

    private fun saveUser(user: User, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response =
                    apiService.registerRequest(user.name, user.email, user.password).awaitResponse()
                if (response.isSuccessful) {
                    onResult(true, "Registrasi Sukses")
                } else {
                    onResult(true, "Registrasi Gagal")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")

            }

        }
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val response = apiService.registerRequest(name, email, password).awaitResponse()
            if (response.isSuccessful) {
                saveUser(User(name, email, password, false), onResult)
            } else {
                onResult(false, "Registrasi gagal")
            }
        }
    }


}
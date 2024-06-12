package com.dicoding.storyapp.login

import androidx.lifecycle.*
import com.dicoding.storyapp.data.User
import com.dicoding.storyapp.data.UserPreference
import com.dicoding.storyapp.remote.ApiService
import kotlinx.coroutines.launch
import retrofit2.await

class LoginViewModel(private val pref: UserPreference, private val apiService: ApiService) :
    ViewModel() {


    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _loggedIn = MutableLiveData<Boolean>()
    val loggedIn: LiveData<Boolean> = _loggedIn


    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiService.loginRequest(email, password).await()
                if (!response.error) {
                    val loginResult = response.loginResult
                        pref.saveToken(loginResult.token)
                        val user = User(
                            email = email,
                            password = password,
                            isLogin = true,
                            name = loginResult.name
                        )
                        pref.saveUser(user)
                        _loggedIn.value = true
                } else {
                    _errorMessage.value = response.message ?: "Terjadi kesalahan"
                }
            } catch (t: Throwable) {
                _errorMessage.value = t.localizedMessage ?: "Terjadi kesalahan"
            }

        }
    }
}


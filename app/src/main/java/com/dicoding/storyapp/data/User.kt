package com.dicoding.storyapp.data

import androidx.room.Entity

@Entity
data class User(
    val name: String,
    val email: String,
    val password: String,
    val isLogin: Boolean
)

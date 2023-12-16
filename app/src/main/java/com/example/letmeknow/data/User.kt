package com.example.letmeknow.data

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val profilePicture: String = ""
)

package com.example.letmeknow.data

data class RecyclerViewData(
    val pollId: String,
    val question: String,
    val author: String,
    var expired: Boolean? = null
)
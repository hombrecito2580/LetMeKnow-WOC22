package com.example.letmeknow.data

import android.net.Uri

data class PollItem(
    val type: String,
    var descriptionData: String,
    var imageUri: Uri,
    var imageUrl: String
)

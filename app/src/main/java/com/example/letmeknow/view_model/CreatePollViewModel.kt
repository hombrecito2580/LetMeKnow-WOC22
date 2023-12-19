package com.example.letmeknow.view_model

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letmeknow.data.PollItem
import com.example.letmeknow.data.PollItemCompressed
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreatePollViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser = firebaseAuth.currentUser
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()

    private val _userLoggedIn = MutableLiveData<Boolean>()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun submitData(
        question: String,
        inputMap: LinkedHashMap<Int, PollItem>,
        optionMap: LinkedHashMap<Int, String>,
        days: Int,
        hours: Int,
        minutes: Int
    ): Boolean {

        if (firebaseUser == null) {
            _userLoggedIn.value = false
            return false
        }
        _userLoggedIn.value = true
        val uid = firebaseUser.uid
        val userPollsRef = firebaseDatabase.getReference("users/$uid/polls")
        val pollsRef = firebaseDatabase.getReference("polls")

        var userName = ""
        val userRef = firebaseDatabase.getReference("users/$uid/name")
        userRef.get().addOnSuccessListener {
            if(it.exists()) {
                userName = it.getValue(String::class.java)!!
            }
        }.await()

        return try {
            val completionDeferred = CompletableDeferred<Boolean>()

            viewModelScope.launch {
                try {
                    val pollIdReference = pollsRef.push()
                    val pollId = pollIdReference.key

                    processImageUploads(inputMap, pollId!!)

                    val updatedInputMap = processInputMap(inputMap)
                    val updatedOptionMap = processOptionMap(optionMap)

                    val utcDeadline = calculateDeadlineTime(days, hours, minutes)
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val utcDeadlineString = utcDeadline.format(formatter)

                    val dataMap = mapOf(
                        "question" to question,
                        "input" to updatedInputMap,
                        "options" to updatedOptionMap,
                        "deadline" to utcDeadlineString,
                        "author" to userName
                    )

                    pollIdReference.setValue(dataMap).await()
                    userPollsRef.child(pollId).setValue(true)
                    completionDeferred.complete(true)
                } catch (e: Exception) {
                    completionDeferred.complete(false)
                }
            }
            completionDeferred.await()
        } catch (e: Exception) {
            false
        }
    }

    private fun processInputMap(inputMap: LinkedHashMap<Int, PollItem>): Map<String, PollItemCompressed> {
        val map = mutableMapOf<String, PollItemCompressed>()
        var itemKey = 0
        for ((_, value) in inputMap) {
            map[itemKey.toString()] =
                PollItemCompressed(value.type, value.descriptionData, value.imageUrl)
            itemKey++
        }
        return map
    }

    private fun processOptionMap(optionMap: LinkedHashMap<Int, String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var itemKey = 0
        for ((_, value) in optionMap) {
            map[itemKey.toString()] = value
            itemKey++
        }
        return map
    }

    private suspend fun processImageUploads(inputMap: LinkedHashMap<Int, PollItem>, pollId: String) {
        val uploadTasks = mutableListOf<Task<Uri>>()
        val storageRef = firebaseStorage.getReference("pollData/$pollId")

        for ((_, input) in inputMap) {
            if (input.type == "image") {
                val imageRef: StorageReference =
                    storageRef.child(System.currentTimeMillis().toString() + ".jpg")
                val task = imageRef.putFile(input.imageUri)
                    .continueWithTask {
                        if (!it.isSuccessful) {
                            input.imageUrl = ""
                        }
                        imageRef.downloadUrl
                    }.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val downloadUrl: Uri? = it.result
                            input.imageUrl = downloadUrl?.toString() ?: ""
                        }
                    }

                uploadTasks.add(task)
            }
        }

        suspendCoroutine { continuation ->
            Tasks.whenAll(uploadTasks)
                .addOnCompleteListener {
                    // Resume the continuation when all tasks are completed
                    continuation.resume(Unit)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDeadlineTime(days: Int, hours: Int, minutes: Int): LocalDateTime {
        val currentUtcTime = LocalDateTime.now(ZoneOffset.UTC)

        val duration = Duration.ofDays(days.toLong())
            .plus(Duration.ofHours(hours.toLong()))
            .plus(Duration.ofMinutes(minutes.toLong()))

        val localDeadline = currentUtcTime.plus(duration)

        return ZonedDateTime.of(localDeadline, ZoneOffset.UTC).toLocalDateTime()
    }
}
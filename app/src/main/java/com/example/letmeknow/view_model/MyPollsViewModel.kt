package com.example.letmeknow.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letmeknow.data.RecyclerViewData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MyPollsViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _myPolls = MutableLiveData<List<RecyclerViewData>>()
    val myPolls: LiveData<List<RecyclerViewData>> get() = _myPolls

    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val dialogFlag: LiveData<Boolean> get() = _dialogFlag

    private val _userLoggedIn = MutableLiveData<Boolean>()
//    val userLoggedIn: LiveData<Boolean> get() = _userLoggedIn

    private var myPollsJob: Job? = null

    fun getMyPolls() {
        myPollsJob?.cancel()

        myPollsJob = viewModelScope.launch {
            if (userId != null) {
                _dialogFlag.value = true
                _userLoggedIn.value = true

                try {
                    val userPollsReference = firebaseDatabase.getReference("users/$userId/polls")

                    val snapshot = suspendCoroutine<DataSnapshot?> { continuation ->
                        userPollsReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                continuation.resume(dataSnapshot)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                continuation.resume(null)
                            }
                        })
                    }

                    val pollIds = snapshot?.children?.mapNotNull { it.key } ?: emptyList()
                    val polls = mutableListOf<RecyclerViewData>()

                    pollIds.forEach { pollId ->
                        val pollReference = firebaseDatabase.getReference("polls/$pollId")

                        val pollSnapshot = suspendCoroutine { continuation ->
                            pollReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    continuation.resume(dataSnapshot)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    continuation.resume(null)
                                }
                            })
                        }

                        val question = pollSnapshot?.child("question")?.getValue(String::class.java) ?: ""
                        val author = pollSnapshot?.child("author")?.getValue(String::class.java) ?: ""

                        val poll = RecyclerViewData(pollId, question, author)
                        polls.add(poll)
                    }

                    _myPolls.value = polls
                } catch (e: Exception) {
                    // Handle exceptions
                } finally {
                    _dialogFlag.value = false
                }
            } else {
                _userLoggedIn.value = false
            }
        }
    }

    fun deletePoll(pollId: String) {
        viewModelScope.launch {
            try {
                _dialogFlag.value = true

                deleteImagesFromStorage(pollId)
                deletePollEntries(pollId)

//                val pollReference = firebaseDatabase.getReference("polls/$pollId")
//                pollReference.removeValue()
//
//                val userPollsReference = firebaseDatabase.getReference("users/$userId/polls/$pollId")
//                userPollsReference.removeValue()

                getMyPolls()
            } catch (e: Exception) {
                // Handle exceptions
            } finally {
                delay(500)
                _dialogFlag.value = false
            }
        }
    }

    private suspend fun deleteImagesFromStorage(pollId: String) {
        // Assuming you have a reference to your Firebase Storage
        val storageReference = FirebaseStorage.getInstance().getReference("pollData/$pollId")

        try {
            // Get a list of items (images) from the storage reference
            val imageList = storageReference.listAll().await()

            // Iterate through the images and delete each one
            imageList.items.forEach { imageReference ->
                imageReference.delete().await()
            }

            Log.d("Storage", "All images deleted successfully")
        } catch (e: Exception) {
            Log.e("Storage", "Error deleting images: ${e.message}")
            // Handle exceptions
        }
    }

    private suspend fun deletePollEntries(pollId: String) {
        // Delete poll entries from Realtime Database
        val pollReference = firebaseDatabase.getReference("polls/$pollId")
        pollReference.removeValue()

        val userPollsReference = firebaseDatabase.getReference("users/$userId/polls/$pollId")
        userPollsReference.removeValue()
    }
}


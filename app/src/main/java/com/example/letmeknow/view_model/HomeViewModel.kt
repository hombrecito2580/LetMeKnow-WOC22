package com.example.letmeknow.view_model

import android.os.Build
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//class HomeViewModel: ViewModel() {
//    private val firebaseAuth = FirebaseAuth.getInstance()
//    private val userId = firebaseAuth.currentUser?.uid
//    private val firebaseDatabase = FirebaseDatabase.getInstance()
//
//    private val _polls = MutableLiveData<List<RecyclerViewData>>()
//    val polls: LiveData<List<RecyclerViewData>> get() = _polls
//
//    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
//    val dialogFlag: LiveData<Boolean> get() = _dialogFlag
//
//    private val _userLoggedIn = MutableLiveData<Boolean>()
////    val userLoggedIn: LiveData<Boolean> get() = _userLoggedIn
//
//    fun getPolls() {
//        viewModelScope.launch {
//            if(userId != null) {
//                _dialogFlag.value = true
//                _userLoggedIn.value = true
//
//                try {
//                    val pollReference = firebaseDatabase.getReference("polls")
//                } catch (e: Exception) {
//                    // Handle exceptions
//                } finally {
//                    _dialogFlag.value = false
//                }
//            } else {
//                _userLoggedIn.value = false
//            }
//        }
//    }
//}

class HomeViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    //    private val userId = firebaseAuth.currentUser?.uid
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _allPolls = MutableLiveData<List<RecyclerViewData>>()
    val allPolls: LiveData<List<RecyclerViewData>> get() = _allPolls

    private val _dialogFlag: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    val dialogFlag: LiveData<Boolean> get() = _dialogFlag

//    private val _userLoggedIn = MutableLiveData<Boolean>()
//    val userLoggedIn: LiveData<Boolean> get() = _userLoggedIn

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllPolls() {
        viewModelScope.launch {
            _dialogFlag.value = true

            try {
                val pollsReference = firebaseDatabase.getReference("polls")

                val snapshot = suspendCoroutine { continuation ->
                    pollsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            continuation.resume(dataSnapshot)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(null)
                        }
                    })
                }

                val polls = mutableListOf<RecyclerViewData>()

                snapshot?.children?.forEach { pollSnapshot ->
                    val pollId = pollSnapshot.key ?: ""
                    val question = pollSnapshot.child("question").getValue(String::class.java) ?: ""
                    val author = pollSnapshot.child("author").getValue(String::class.java) ?: ""
                    val deadline = pollSnapshot.child("deadline").getValue(String::class.java)

                    if (deadline != null && !hasDeadlinePassed(deadline)) {
                        val poll = RecyclerViewData(pollId, question, author)
                        polls.add(poll)
                    }

                }

                _allPolls.value = polls
            } catch (e: Exception) {
                // Handle exceptions
            } finally {
                _dialogFlag.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hasDeadlinePassed(deadline: String): Boolean {
        val formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))

        return try {
            val currentUtcTime = LocalDateTime.now(ZoneId.of("UTC"))
            val deadlineDate = LocalDateTime.parse(deadline, formatter)

            // Compare the current UTC time with the deadline
            currentUtcTime.isAfter(deadlineDate)
        } catch (e: Exception) {
            // Handle parsing exceptions
            e.printStackTrace()
            false // Assume deadline hasn't passed in case of parsing issues
        }
    }
}
package com.example.letmeknow.view_model

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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//class MyPollsViewModel: ViewModel() {
//    private val firebaseAuth = FirebaseAuth.getInstance()
//    private val userId = firebaseAuth.currentUser?.uid
//    private val firebaseDatabase = FirebaseDatabase.getInstance()
//
//    private val _myPolls = MutableLiveData<List<RecyclerViewData>>()
//    val myPolls: LiveData<List<RecyclerViewData>> get() = _myPolls
//
//    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
//    val dialogFlag: LiveData<Boolean> get() = _dialogFlag
//
//    fun getMyPolls() {
//        if(userId != null) {
//            _dialogFlag.value = true
//
//            val userPollsReference = firebaseDatabase.getReference("users/$userId/polls")
//
//            userPollsReference.addListenerForSingleValueEvent(object: ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val pollIds = snapshot.children.map { it.key ?: "" }
//                    val polls = mutableListOf<RecyclerViewData>()
//
//                    val latch = CountDownLatch(pollIds.size)
//
//                    for(pollId in pollIds) {
//                        val pollReference = FirebaseDatabase.getInstance().getReference("polls/$pollId")
//
//                        pollReference.addListenerForSingleValueEvent(object: ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                val question = snapshot.child("question").getValue(String::class.java)!!
//                                val author = snapshot.child("author").getValue(String::class.java)!!
//                                val poll = RecyclerViewData(pollId, question, author)
//
//                                polls.add(poll)
//                                latch.countDown()
//
//                                if (latch.count == 0L) {
//                                    // All tasks are complete, update LiveData
//                                    _myPolls.value = polls
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                latch.countDown()
//                            }
//
//                        })
//                    }
//
//                    latch.await(10, TimeUnit.SECONDS)
//                    _dialogFlag.value = false
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    _dialogFlag.value = false
//                }
//
//            })
//        }
//    }
//
//}

class MyPollsViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _myPolls = MutableLiveData<List<RecyclerViewData>>()
    val myPolls: LiveData<List<RecyclerViewData>> get() = _myPolls

    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val dialogFlag: LiveData<Boolean> get() = _dialogFlag

    fun getMyPolls() {
        viewModelScope.launch {
            if (userId != null) {
                _dialogFlag.value = true

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
            }
        }
    }
}


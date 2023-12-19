package com.example.letmeknow.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.letmeknow.data.PollItemCompressed
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.storage.FirebaseStorage

class AnswerPollViewModel: ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser = firebaseAuth.currentUser
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val dialogFlag: LiveData<Boolean> get() = _dialogFlag

    var question = ""
    var input = ArrayList<PollItemCompressed>()
    var options = ArrayList<String>()

    fun loadData(pollId: String, completed: (Boolean) -> Unit) {
        val pollRef = firebaseDatabase.getReference("polls").child(pollId)

        val questionRef = pollRef.child("question")
        val inputRef = pollRef.child("input")
        val optionsRef = pollRef.child("options")

        var questionLoaded = false
        var inputLoaded = false
        var optionsLoaded = false

        var errorOccurred = false

        fun checkCompletion() {
            if (errorOccurred) {
                completed(false)
            } else if (questionLoaded && inputLoaded && optionsLoaded) {
                completed(true)
            }
        }

        questionRef.get().addOnCompleteListener { questionTask ->
            if (questionTask.isSuccessful) {
                question = questionTask.result?.getValue(String::class.java)!!
                questionLoaded = true
                checkCompletion()
            } else {
                errorOccurred = true
                checkCompletion()
            }
        }

        inputRef.get().addOnCompleteListener { inputTask ->
            if (inputTask.isSuccessful) {
                input = inputTask.result?.getValue(object : GenericTypeIndicator<ArrayList<PollItemCompressed>>() {})!!
                println("Value of 'input': $input")
                inputLoaded = true
                checkCompletion()
            } else {
                println("Error retrieving 'input': ${inputTask.exception?.message}")
                errorOccurred = true
                checkCompletion()
            }
        }

        optionsRef.get().addOnCompleteListener { optionsTask ->
            if (optionsTask.isSuccessful) {
                options = optionsTask.result?.getValue(object : GenericTypeIndicator<ArrayList<String>>() {})!!
                println("Value of 'options': $options")
                optionsLoaded = true
                checkCompletion()
            } else {
                println("Error retrieving 'options': ${optionsTask.exception?.message}")
                errorOccurred = true
                checkCompletion()
            }
        }
    }

    fun recordResponse(pollId: String, index: Int, completed: (Boolean) -> Unit) {
        val pollRef = firebaseDatabase.getReference("polls").child(pollId)
        val responseRef = pollRef.child("response")

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            responseRef.child(userId).setValue(index)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        completed(true)
                    } else {
                        completed(false)
                    }
                }
        } else {
            // User is not authenticated
            completed(false)
        }
    }
}
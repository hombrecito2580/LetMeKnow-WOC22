package com.example.letmeknow.view_model

import androidx.lifecycle.ViewModel
import com.example.letmeknow.data.MyPair
import com.example.letmeknow.data.PollItemCompressed
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.getValue

class PollAnalysisViewModel: ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser = firebaseAuth.currentUser
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    var question = ""
    var input = ArrayList<PollItemCompressed>()
    private var options = ArrayList<String>()
    private var responses = HashMap<String, Int>()
    var optionPair = ArrayList<MyPair>()

    fun loadData(pollId: String, completed: (Boolean) -> Unit) {
        val pollRef = firebaseDatabase.getReference("polls").child(pollId)

        val questionRef = pollRef.child("question")
        val inputRef = pollRef.child("input")
        val optionsRef = pollRef.child("options")
        val responseRef = pollRef.child("response")

        var questionLoaded = false
        var inputLoaded = false
        var optionsLoaded = false
        var responsesLoaded = false

        var errorOccurred = false

        fun checkCompletion() {
            if (errorOccurred) {
                completed(false)
            } else if (questionLoaded && inputLoaded && optionsLoaded && responsesLoaded) {

                for(option in options) {
                    val pair = MyPair(option)
                    optionPair.add(pair)
                }

                for(response in responses) {
                    optionPair[response.value].second++
                }

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

        responseRef.get().addOnCompleteListener { responseTask ->
            if(responseTask.isSuccessful) {
                responses = responseTask.result?.getValue(object : GenericTypeIndicator<HashMap<String, Int>>() {})!!

                println("Value of 'responses' : $responses")
                println("\n\n\n\n\n\n\n\n\n\n\n")
                responsesLoaded = true
                checkCompletion()
            }
        }
    }
}
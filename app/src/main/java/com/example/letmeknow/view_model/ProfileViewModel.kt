package com.example.letmeknow.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileViewModel: ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = Firebase.database.reference
    private val firebaseUser = firebaseAuth.currentUser

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> get() = _userEmail

    private val _userProfile = MutableLiveData<String>()
    val userProfile: LiveData<String> get() = _userProfile

    private val _userLoggedIn = MutableLiveData<Boolean>()
    val userLoggedIn: LiveData<Boolean> get() = _userLoggedIn

    private val _firebaseProfile = MutableLiveData<String>()
    val firebaseProfile: LiveData<String> get() = _firebaseProfile

    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val dialogFlag: LiveData<Boolean> get() = _dialogFlag

    fun loadDataFromFirebase() {
        _dialogFlag.value = true

        if(firebaseUser == null) {
            _userLoggedIn.value = false
            _dialogFlag.value = false
        }
        else {
            _userLoggedIn.value = true
            val uid = firebaseUser.uid
            firebaseDatabase.child("users").child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _dialogFlag.value = true

                    if (snapshot.exists()) {
                        _userProfile.value = snapshot.child("profilePicture").value.toString()
                        _userName.value = snapshot.child("name").value.toString()
                        _userEmail.value = snapshot.child("email").value.toString()
                    }
                    _dialogFlag.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _dialogFlag.value = false
                }
            })
        }

    }

    fun signOut(): Boolean {
        return try {
            FirebaseAuth.getInstance().signOut()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
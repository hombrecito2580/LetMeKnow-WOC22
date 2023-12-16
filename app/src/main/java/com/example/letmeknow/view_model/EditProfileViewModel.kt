package com.example.letmeknow.view_model

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class EditProfileViewModel: ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = Firebase.database.reference
    private val firebaseUser = firebaseAuth.currentUser
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val storageRef = firebaseStorage.reference

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _userProfile = MutableLiveData<String>()
    val userProfile: LiveData<String> get() = _userProfile

    private val _userAbout = MutableLiveData<String>()
    val userAbout: LiveData<String> get() = _userAbout

    private val _userLoggedIn = MutableLiveData<Boolean>()
    val userLoggedIn: LiveData<Boolean> get() = _userLoggedIn

    private val _dialogFlag: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val dialogFlag: LiveData<Boolean> get() = _dialogFlag

    private var dataFetched = false

    fun uploadImage(uri: Uri) {
        val imageRef: StorageReference = storageRef.child("profiles/" + System.currentTimeMillis() + ".jpg")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {

        } .addOnFailureListener {

        }

        uploadTask.continueWithTask {task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            imageRef.downloadUrl
        } .addOnCompleteListener {task ->
            if(task.isSuccessful) {
                val downloadUrl: Uri? = task.result
                val downloadUrlString: String = downloadUrl?.toString() ?: ""
                updateProfile(downloadUrlString)
            }
            else {

            }
        }
    }

    fun uploadImage(bitmap: Bitmap) {
        val imageRef: StorageReference = storageRef.child("profiles/" + System.currentTimeMillis() + ".jpg")

        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        val data: ByteArray = outStream.toByteArray()

        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {

        } .addOnFailureListener {

        }

        uploadTask.continueWithTask {task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            imageRef.downloadUrl
        } .addOnCompleteListener {task ->
            if(task.isSuccessful) {
                val downloadUrl: Uri? = task.result
                val downloadUrlString: String = downloadUrl?.toString() ?: ""
                updateProfile(downloadUrlString)
            }
            else {

            }
        }
    }

    private fun updateProfile(profile: String) {
        if (firebaseUser == null) {
            _userLoggedIn.value = false
        }
        else {
            _userLoggedIn.value = true
            val uid = firebaseUser.uid

            var initialProfile = ""
            firebaseDatabase.child("users").child(uid).child("profilePicture").get()
                .addOnSuccessListener {
                    initialProfile = it.value.toString()
                }

            val profileMap = mapOf("profilePicture" to profile)
            firebaseDatabase.child("users").child(uid).updateChildren(profileMap)
                .addOnSuccessListener {
                    if (initialProfile.isNotEmpty()) {
                        val imageRef: StorageReference =
                            firebaseStorage.getReferenceFromUrl(initialProfile)
                        imageRef.delete()
                    }
                }
                .addOnFailureListener {

                }
        }
    }

    fun deleteProfile() {
        updateProfile("")
//        if (firebaseUser == null) {
//            _userLoggedIn.value = false
//        }
//        else {
//            _userLoggedIn.value = true
//            val uid = firebaseUser.uid
//
//            var initialProfile = ""
//            firebaseDatabase.child("users").child(uid).child("profilePicture").get()
//                .addOnSuccessListener {
//                    initialProfile = it.value.toString()
//                }
//
//            val profileMap = mapOf("profilePicture" to "")
//            firebaseDatabase.child("users").child(uid).updateChildren(profileMap)
//                .addOnSuccessListener {
//                    if (initialProfile.isNotEmpty()) {
//                        val imageRef: StorageReference =
//                            firebaseStorage.getReferenceFromUrl(initialProfile)
//                        imageRef.delete()
//                    }
//                }
//                .addOnFailureListener {
//
//                }
//        }
    }

    fun updateName(name: String) {
        if(firebaseUser == null) {
            _userLoggedIn.value = false
        }
        else {
            _userLoggedIn.value = true
            val uid = firebaseUser.uid
            val nameMap = mapOf("name" to name)
            firebaseDatabase.child("users").child(uid).updateChildren(nameMap)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
        }
    }

    fun updateAbout(about: String) {
        if(firebaseUser == null) {
            _userLoggedIn.value = false
        }
        else {
            _userLoggedIn.value = true
            val uid = firebaseUser.uid
            val aboutMap = mapOf("about" to about)
            firebaseDatabase.child("users").child(uid).updateChildren(aboutMap)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
        }
    }

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
                        _userAbout.value = snapshot.child("about").value.toString()
                    }

                    _dialogFlag.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error if needed
                    _dialogFlag.value = false
                }
            })
        }
    }
}
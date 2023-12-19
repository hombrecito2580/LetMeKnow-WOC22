package com.example.letmeknow.view_model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser = firebaseAuth.currentUser

    fun changePassword(password: String, newPassword: String, onComplete: (Boolean) -> Unit) {
        if (firebaseUser != null) {
            val email = firebaseUser.email!!

            val credential: AuthCredential = EmailAuthProvider.getCredential(email, password)

            firebaseUser.reauthenticate(credential)
                .addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        firebaseUser.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                val isPasswordUpdated = updatePasswordTask.isSuccessful
                                onComplete(isPasswordUpdated)
                            }
                    } else {
                        onComplete(false)
                    }
                }

        } else {
            onComplete(false)
        }
    }
}
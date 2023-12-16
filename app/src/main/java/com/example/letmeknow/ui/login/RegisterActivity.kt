package com.example.letmeknow.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.letmeknow.R
import com.example.letmeknow.data.User
import com.example.letmeknow.databinding.ActivityRegisterBinding
import com.example.letmeknow.ui.main.MainActivity
import com.example.letmeknow.utils.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity(), View.OnFocusChangeListener {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.etName.onFocusChangeListener = this
        binding.etEmail.onFocusChangeListener = this
        binding.etPassword.onFocusChangeListener = this
        binding.etConfirmPassword.onFocusChangeListener = this

        binding.btnRegister.setOnClickListener {
            if (validateName() && validateEmail() && validatePassword() && validateConfirmPassword() && validatePasswordAndConfirmPassword()) {
                createAccountAndRedirect()
            }
        }
    }

    private fun createAccountAndRedirect() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { createTask ->
                if (createTask.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser!!
                    val profileReference = FirebaseDatabase.getInstance().getReference("users")
                    val userDetails = User(uid = firebaseUser.uid, name = name, email = email)

                    profileReference.child(firebaseUser.uid).setValue(userDetails)
                        .addOnCompleteListener { storeTask ->
                            if (storeTask.isSuccessful) {
                                showCustomToast("Account Successfully Created!")

                                val intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            } else {
                                showCustomToast("Registration Failed. Please Try Again.")
                                Log.d("STORAGE_ERROR", storeTask.exception.toString())
                            }
                        }
                } else {
                    showCustomToast("Registration Failed. Please Try Again.")
                    Log.d("CREATE_ERROR", createTask.exception.toString())
                }
            }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun validateName(): Boolean {
        var error: String? = null
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            error = "Please enter your name."
        }

        if (error != null) {
            binding.tvNameError.visibility = View.VISIBLE
            binding.tvNameError.text = error
        } else {
            binding.tvNameError.visibility = View.GONE
        }

        return error == null
    }

    private fun validateEmail(): Boolean {
        var error: String? = null
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            error = "Please enter your email."
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error = "Invalid email address."
        }

        if (error != null) {
            binding.tvEmailError.visibility = View.VISIBLE
            binding.tvEmailError.text = error
        } else {
            binding.tvEmailError.visibility = View.GONE
        }

        return error == null
    }

    private fun validatePassword(): Boolean {
        var error: String? = null
        val password = binding.etPassword.text.toString().trim()
        if (password.isEmpty()) {
            error = "Please enter your password."
        } else if (password.length < 6) {
            error = "Password must contain at least 6 characters."
        }

        if (error != null) {
            binding.tvPasswordError.visibility = View.VISIBLE
            binding.tvPasswordError.text = error
        } else {
            binding.tvPasswordError.visibility = View.GONE
        }

        return error == null
    }

    private fun validateConfirmPassword(): Boolean {
        var error: String? = null
        val password = binding.etConfirmPassword.text.toString().trim()
        if (password.isEmpty()) {
            error = "Please confirm your password."
        }

        if (error != null) {
            binding.tvConfirmPasswordError.visibility = View.VISIBLE
            binding.tvConfirmPasswordError.text = error
        } else {
            binding.tvConfirmPasswordError.visibility = View.GONE
        }

        return error == null
    }

    private fun validatePasswordAndConfirmPassword(): Boolean {
        var error: String? = null
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        if (password != confirmPassword) {
            error = "Passwords do not match."
        }

        if (error != null) {
            binding.tvConfirmPasswordError.visibility = View.VISIBLE
            binding.tvConfirmPasswordError.text = error
        } else {
            binding.tvConfirmPasswordError.visibility = View.GONE
        }

        return error == null
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.etName -> {
                    if (hasFocus) {
                        if (binding.tvNameError.visibility != View.GONE) {
                            binding.tvNameError.visibility = View.GONE
                        }
                    } else {
                        validateName()
                    }
                }

                R.id.etEmail -> {
                    if (hasFocus) {
                        if (binding.tvEmailError.visibility != View.GONE) {
                            binding.tvEmailError.visibility = View.GONE
                        }
                    } else {
                        validateEmail()
                    }
                }

                R.id.etPassword -> {
                    if (hasFocus) {
                        if (binding.tvPasswordError.visibility != View.GONE) {
                            binding.tvPasswordError.visibility = View.GONE
                        }
                    } else {
                        validatePassword()
                    }
                }

                R.id.etConfirmPassword -> {
                    if (hasFocus) {
                        if (binding.tvConfirmPasswordError.visibility != View.GONE) {
                            binding.tvConfirmPasswordError.visibility = View.GONE
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword()) {
                            validatePasswordAndConfirmPassword()
                        }
                    }
                }
            }
        }
    }
}
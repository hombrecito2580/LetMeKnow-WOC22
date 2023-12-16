package com.example.letmeknow.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.letmeknow.R
import com.example.letmeknow.databinding.ActivityLoginBinding
import com.example.letmeknow.ui.main.MainActivity
import com.example.letmeknow.utils.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity(), View.OnFocusChangeListener {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            if (validateEmail() && validatePassword()) {
                loginAndRedirect()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loginAndRedirect() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { login ->
            if (login.isSuccessful) {
                showCustomToast("Login Successful!")
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                showCustomToast("Login Failed. Please Try Again.")
            }
        }
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

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
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
            }
        }
    }
}
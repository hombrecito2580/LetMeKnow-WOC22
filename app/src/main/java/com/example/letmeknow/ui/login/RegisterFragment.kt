package com.example.letmeknow.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.letmeknow.R
import com.example.letmeknow.data.User
import com.example.letmeknow.databinding.FragmentRegisterBinding
import com.example.letmeknow.ui.main.MainActivity
import com.example.letmeknow.utils.showCustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment(), View.OnFocusChangeListener {
    private var _binding: FragmentRegisterBinding? = null
    private val binding: FragmentRegisterBinding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                                requireActivity().showCustomToast("Account Successfully Created!")

                                val intent = Intent(context, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            } else {
                                requireActivity().showCustomToast("Registration Failed. Please Try Again.")
                                Log.d("STORAGE_ERROR", storeTask.exception.toString())
                            }
                        }
                } else {
                    requireActivity().showCustomToast("Registration Failed. Please Try Again.")
                    Log.d("CREATE_ERROR", createTask.exception.toString())
                }
            }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
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
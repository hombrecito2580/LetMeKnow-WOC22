package com.example.letmeknow.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentLoginBinding
import com.example.letmeknow.ui.main.MainActivity
import com.example.letmeknow.utils.showCustomToast
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(), View.OnFocusChangeListener {
    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            if (validateEmail() && validatePassword()) {
                loginAndRedirect()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loginAndRedirect() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { login ->
            if (login.isSuccessful) {
                requireActivity().showCustomToast("Login Successful!")
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            } else {
                requireActivity().showCustomToast("Login Failed. Please Try Again.")
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
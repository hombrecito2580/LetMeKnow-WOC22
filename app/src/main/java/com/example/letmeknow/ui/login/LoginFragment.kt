package com.example.letmeknow.ui.login

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.progress_bar)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etEmail.onFocusChangeListener = this
        binding.etPassword.onFocusChangeListener = this

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            if (validateEmail() && validatePassword()) {
                loginAndRedirect()
            }
        }

        binding.btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.forgot_password_dialog, null)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)
        val sendEmailButton = dialogView.findViewById<Button>(R.id.sendEmailButton)
        val emailErrorText = dialogView.findViewById<TextView>(R.id.emailErrorText)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val forgotDialog = builder.show()

        sendEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                emailErrorText.visibility = View.VISIBLE
                emailErrorText.text = "Please enter your email"
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailErrorText.visibility = View.VISIBLE
                emailErrorText.text = "Invalid email"
            } else {
                emailErrorText.visibility = View.GONE
                dialog.show()
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            requireActivity().showCustomToast("Verification Email Sent!")
                        } else {
                            requireActivity().showCustomToast("Couldn't send verification email...")
                        }
                        forgotDialog.dismiss()
                        dialog.dismiss()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loginAndRedirect() {
        dialog.show()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { login ->
            if (login.isSuccessful) {
                requireActivity().showCustomToast("Login Successful!")
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                dialog.dismiss()
                startActivity(intent)
            } else {
                dialog.dismiss()
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
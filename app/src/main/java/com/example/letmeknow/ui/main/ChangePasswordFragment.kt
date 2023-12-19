package com.example.letmeknow.ui.main

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentChangePasswordBinding
import com.example.letmeknow.utils.showCustomToast
import com.example.letmeknow.view_model.ChangePasswordViewModel

class ChangePasswordFragment : Fragment(), View.OnFocusChangeListener {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ChangePasswordViewModel
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ChangePasswordViewModel::class.java]

        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.progress_bar)
        dialog.setCancelable(false)
        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
        dialog.window?.attributes = layoutParams
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.bg)))
        }
        // ColorDrawable(0)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etCurrentPassword.onFocusChangeListener = this
        binding.etNewPassword.onFocusChangeListener = this
        binding.etConfirmNewPassword.onFocusChangeListener = this

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            if(validateCurrentPassword() && validateNewPassword() && validateConfirmNewPassword() && validateNewPasswordAndConfirmNewPassword()) {
                dialog.show()

                val currentPassword = binding.etCurrentPassword.text.toString().trim()
                val newPassword = binding.etNewPassword.text.toString().trim()

                viewModel.changePassword(currentPassword, newPassword) { isPasswordUpdated ->
                    if(isPasswordUpdated) {
                        requireActivity().showCustomToast("Password successfully changed.")
                        dialog.dismiss()
                        findNavController().popBackStack()
                    } else {
                        requireActivity().showCustomToast("Couldn't change the password.")
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.etCurrentPassword -> {
                    if (hasFocus) {
                        if (binding.tvCurrentPasswordError.visibility != View.GONE) {
                            binding.tvCurrentPasswordError.visibility = View.GONE
                        }
                    } else {
                        validateCurrentPassword()
                    }
                }

                R.id.etNewPassword -> {
                    if (hasFocus) {
                        if (binding.tvNewPassword.visibility != View.GONE) {
                            binding.tvNewPassword.visibility = View.GONE
                        }
                    } else {
                        validateNewPassword()
                    }
                }

                R.id.etConfirmNewPassword -> {
                    if (hasFocus) {
                        if (binding.tvConfirmNewPasswordError.visibility != View.GONE) {
                            binding.tvConfirmNewPasswordError.visibility = View.GONE
                        }
                    } else {
                        if (validateConfirmNewPassword() && validateNewPassword()) {
                            validateNewPasswordAndConfirmNewPassword()
                        }
                    }
                }
            }
        }
    }

    private fun validateCurrentPassword(): Boolean {
        var error: String? = null
        val password = binding.etCurrentPassword.text.toString().trim()
        if (password.isEmpty()) {
            error = "Please enter your password."
        }

        if (error != null) {
            binding.tvCurrentPasswordError.visibility = View.VISIBLE
            binding.tvCurrentPasswordError.text = error
        } else {
            binding.tvCurrentPasswordError.visibility = View.GONE
        }

        return error == null
    }

    private fun validateNewPassword(): Boolean {
        var error: String? = null
        val password = binding.etNewPassword.text.toString().trim()
        if (password.isEmpty()) {
            error = "Please enter your password."
        } else if (password.length < 6) {
            error = "Password must contain at least 6 characters."
        }

        if (error != null) {
            binding.tvNewPasswordError.visibility = View.VISIBLE
            binding.tvNewPasswordError.text = error
        } else {
            binding.tvNewPasswordError.visibility = View.GONE
        }

        return error == null
    }

    private fun validateConfirmNewPassword(): Boolean {
        var error: String? = null
        val password = binding.etConfirmNewPassword.text.toString().trim()
        if (password.isEmpty()) {
            error = "Please confirm your password."
        }

        if (error != null) {
            binding.tvConfirmNewPasswordError.visibility = View.VISIBLE
            binding.tvConfirmNewPasswordError.text = error
        } else {
            binding.tvConfirmNewPasswordError.visibility = View.GONE
        }

        return error == null
    }

    private fun validateNewPasswordAndConfirmNewPassword(): Boolean {
        var error: String? = null
        val password = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()
        if (password != confirmPassword) {
            error = "Passwords do not match."
        }

        if (error != null) {
            binding.tvConfirmNewPasswordError.visibility = View.VISIBLE
            binding.tvConfirmNewPasswordError.text = error
        } else {
            binding.tvConfirmNewPasswordError.visibility = View.GONE
        }

        return error == null
    }
}
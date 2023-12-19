package com.example.letmeknow.ui.main

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentProfileBinding
import com.example.letmeknow.ui.login.AuthActivity
import com.example.letmeknow.utils.LogoutDialog
import com.example.letmeknow.utils.SharedPreferenceManager
import com.example.letmeknow.utils.ThemeAlertDialog
import com.example.letmeknow.utils.ThemeHelper
import com.example.letmeknow.utils.showCustomToast
import com.example.letmeknow.view_model.ProfileViewModel

class ProfileFragment : Fragment() {
    private lateinit var profileViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

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

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        profileViewModel.userName.observe(viewLifecycleOwner) { userName ->
            binding.tvName.text = userName
        }

        profileViewModel.userEmail.observe(viewLifecycleOwner) { userEmail ->
            binding.tvEmail.text = userEmail
        }

        profileViewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            if(userProfile.isNotEmpty()) {
                Glide.with(this)
                    .load(userProfile)
                    .into(binding.ivProfile)
            }
        }

        profileViewModel.dialogFlag.observe(viewLifecycleOwner) { flag ->
            if(flag) {
                dialog.show()
            }
            else {
                dialog.dismiss()
            }
        }

        profileViewModel.loadDataFromFirebase()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment, null)
        }

        binding.btnThemes.setOnClickListener {
            showThemeAlertDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment, null)
        }
    }

    private fun showLogoutDialog() {
        val logoutDialog = LogoutDialog(requireContext()) {
            if(profileViewModel.signOut()) {
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                if (requireActivity() is MainActivity) {
                    requireActivity().finish()
                }
            } else {
                requireActivity().showCustomToast("Failed to log out!")
            }
        }

        logoutDialog.show()
    }

    private fun showThemeAlertDialog() {
        val sharedPreferenceManager = SharedPreferenceManager(requireContext())

        val checkedTheme = sharedPreferenceManager.theme
        val themeAlertDialog = ThemeAlertDialog(requireContext(), checkedTheme) { selectedTheme ->
            applyTheme(selectedTheme)
            sharedPreferenceManager.theme = selectedTheme
        }

        themeAlertDialog.show()
    }

    private fun applyTheme(theme: Int) {
        ThemeHelper.applyTheme(when (theme) {
            ThemeAlertDialog.LIGHT_MODE -> ThemeHelper.LIGHT_MODE
            ThemeAlertDialog.DARK_MODE -> ThemeHelper.DARK_MODE
            ThemeAlertDialog.DEFAULT_MODE -> ThemeHelper.DEFAULT_MODE
            else -> ThemeHelper.DEFAULT_MODE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
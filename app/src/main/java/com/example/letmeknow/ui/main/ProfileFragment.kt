package com.example.letmeknow.ui.main

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentProfileBinding
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
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

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

        binding.etEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment, null)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
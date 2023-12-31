package com.example.letmeknow.ui.main

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentEditProfileBinding
import com.example.letmeknow.view_model.EditProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class EditProfileFragment : Fragment() {
    private lateinit var editProfileViewModel: EditProfileViewModel
    private var _binding: FragmentEditProfileBinding? = null
    private val binding: FragmentEditProfileBinding get() = _binding!!
    private lateinit var dialog: Dialog

    private var profileEdited: Boolean = false
    private var nameEdited: Boolean = false
    private var initialName: String = ""
    private var aboutEdited: Boolean = false
    private var initialAbout: String = ""

    private var latestSelectedImageUri: Uri? = null
    private var latestSelectedImageBitmap: Bitmap? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                latestSelectedImageBitmap = result.data?.extras?.get("data") as Bitmap?
                latestSelectedImageUri = null
                profileEdited = true
                updateImageView(latestSelectedImageBitmap)
                updateSubmitButtonState()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                latestSelectedImageUri = result.data?.data
                latestSelectedImageBitmap = null
                profileEdited = true
                updateImageView(latestSelectedImageUri)
                updateSubmitButtonState()
            }
        }

//    private val galleryPermissionRequestCode = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

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

        editProfileViewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        editProfileViewModel.loadDataFromFirebase()

        editProfileViewModel.userName.observe(viewLifecycleOwner) { userName ->
            initialName = userName
            binding.etName.setText(userName)
        }

        editProfileViewModel.userAbout.observe(viewLifecycleOwner) { userAbout ->
            initialAbout = userAbout
            binding.etAbout.setText(userAbout)
        }

        editProfileViewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            if(userProfile.isNotEmpty()) {
                Glide.with(this)
                    .load(userProfile)
                    .into(binding.ivProfile)
            }
            else {
                binding.ivProfile.setImageResource(R.drawable.ic_user_male)
            }
        }

        editProfileViewModel.dialogFlag.observe(viewLifecycleOwner) { flag ->
            if(flag) {
                dialog.show()
            }
            else {
                dialog.dismiss()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.isClickable = false
        binding.btnSubmit.isActivated = false



        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nameEdited = s.toString() != initialName
            }

            override fun afterTextChanged(s: Editable?) {
                updateSubmitButtonState()
            }
        })

        binding.etAbout.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aboutEdited = s.toString() != initialAbout
            }

            override fun afterTextChanged(s: Editable?) {
                updateSubmitButtonState()
            }
        })

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEditProfile.setOnClickListener {
            showBottomSheet()
        }

        binding.btnSubmit.setOnClickListener {
            if(profileEdited) {
                if(latestSelectedImageUri != null) {
                    editProfileViewModel.uploadImage(latestSelectedImageUri!!)
                }
                else if(latestSelectedImageBitmap != null) {
                    editProfileViewModel.uploadImage(latestSelectedImageBitmap!!)
                }
            }

            if(nameEdited) {
                editProfileViewModel.updateName(binding.etName.text.toString().trim())
            }

            if(aboutEdited) {
                editProfileViewModel.updateAbout(binding.etAbout.text.toString().trim())
            }
        }
    }

    private fun showBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(bottomSheetView)

        val btnCamera = bottomSheetView.findViewById<ImageView>(R.id.btnCamera)
        val btnGallery = bottomSheetView.findViewById<ImageView>(R.id.btnGallery)
        val btnDelete = bottomSheetView.findViewById<ImageView>(R.id.btnDelete)

        btnCamera.setOnClickListener {
            openCamera()
            dialog.dismiss()
        }

        btnGallery.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            editProfileViewModel.deleteProfile()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun updateImageView(imageBitmap: Bitmap?) {
        if (imageBitmap != null) {
            Glide.with(this)
                .load(imageBitmap)
                .centerInside()
                .into(binding.ivProfile)
        }
    }

    private fun updateImageView(imageUri: Uri?) {
        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .centerInside()
                .into(binding.ivProfile)
        }
    }

    fun updateSubmitButtonState() {
        if (profileEdited || nameEdited || aboutEdited) {
            binding.btnSubmit.isActivated = true
            binding.btnSubmit.isClickable = true
        } else {
            binding.btnSubmit.isActivated = false
            binding.btnSubmit.isClickable = false
        }
    }
}
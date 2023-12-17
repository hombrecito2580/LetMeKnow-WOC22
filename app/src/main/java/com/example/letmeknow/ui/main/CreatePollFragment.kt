package com.example.letmeknow.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.letmeknow.R
import com.example.letmeknow.data.PollItem
import com.example.letmeknow.databinding.FragmentCreatePollBinding
import com.example.letmeknow.utils.showCustomToast
import com.example.letmeknow.view_model.CreatePollViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreatePollFragment : Fragment(), View.OnFocusChangeListener {
    private var _binding: FragmentCreatePollBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CreatePollViewModel
    private lateinit var dialog: Dialog

    private val inputMap = linkedMapOf<Int, PollItem>()
    private val optionMap = linkedMapOf<Int, String>()

    private var indDescription = 0
    private var indOption = 0
    private var selectedImageIndex = -1
    private var selectedImage: ImageView? = null

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                inputMap[selectedImageIndex]!!.imageUri = result.data?.data!!
                selectedImage!!.setImageURI(result.data?.data!!)
            }
            selectedImageIndex = -1
            selectedImage = null
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePollBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[CreatePollViewModel::class.java]

        dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.progress_bar)
        dialog.setCancelable(false)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.daysPicker.minValue = 0
        binding.daysPicker.maxValue = 30

        binding.hoursPicker.minValue = 0
        binding.hoursPicker.maxValue = 23

        binding.minutesPicker.minValue = 0
        binding.minutesPicker.maxValue = 59

        binding.etQuestion.onFocusChangeListener = this
        binding.etOption1.onFocusChangeListener = this
        binding.etOption2.onFocusChangeListener = this

        inputMap[indDescription] = PollItem("description", "", Uri.parse(""), "")
        indDescription++
        binding.etDescription.addTextChangedListener {
            inputMap[0]!!.descriptionData = it.toString()
        }

        optionMap[indOption] = binding.etOption1.text.toString()
        indOption++
        binding.etOption1.addTextChangedListener {
            optionMap[0] = it.toString()
        }

        optionMap[indOption] = binding.etOption2.text.toString()
        indOption++
        binding.etOption2.addTextChangedListener {
            optionMap[1] = it.toString()
        }

        binding.btnAddImage.setOnClickListener {
            val index = indDescription
            inputMap[index] = PollItem("image", "", Uri.parse(""), "")
            indDescription++

            val imageLayout = LayoutInflater.from(context).inflate(R.layout.layout_add_image, null)
            binding.containerDescription.addView(imageLayout)

            val image = imageLayout.findViewById<ImageView>(R.id.ivDescription)
            image.setOnClickListener {
                selectedImage = image
                selectedImageIndex = index
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }

            imageLayout.findViewById<Button>(R.id.btnRemove).setOnClickListener {
                inputMap.remove(index)
                binding.containerDescription.removeView(imageLayout)
            }
        }

        binding.btnAddDescription.setOnClickListener {
            val index = indDescription
            inputMap[index] = PollItem("description", "", Uri.parse(""), "")
            indDescription++

            val descriptionLayout =
                LayoutInflater.from(context).inflate(R.layout.layout_add_description, null)
            binding.containerDescription.addView(descriptionLayout)

            descriptionLayout.findViewById<TextInputEditText>(R.id.etDescription)
                .addTextChangedListener {
                    inputMap[index]!!.descriptionData = it.toString()
                }

            descriptionLayout.findViewById<Button>(R.id.btnRemove).setOnClickListener {
                inputMap.remove(index)
                binding.containerDescription.removeView(descriptionLayout)
            }
        }

        binding.btnAddOption.setOnClickListener {
            val index = indOption
            optionMap[index] = ""
            indOption++

            val optionLayout =
                LayoutInflater.from(context).inflate(R.layout.layout_add_option, null)
            binding.containerOption.addView(optionLayout)

            optionLayout.findViewById<TextInputEditText>(R.id.etOption).addTextChangedListener {
                optionMap[index] = it.toString()
            }

            optionLayout.findViewById<Button>(R.id.btnRemove).setOnClickListener {
                optionMap.remove(index)
                binding.containerOption.removeView(optionLayout)
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (validateQuestion() && validateOption1() && validateOption2() && validateDeadline()) {
                lifecycleScope.launch {
                    dialog.show()



                    if (viewModel.submitData(
                            binding.etQuestion.text.toString(),
                            inputMap,
                            optionMap,
                            binding.daysPicker.value,
                            binding.hoursPicker.value,
                            binding.minutesPicker.value
                        )
                    ) {
                        dialog.dismiss()
                        findNavController().popBackStack(R.id.createPollFragment, true)
                        findNavController().navigate(R.id.createPollFragment)
                        requireActivity().showCustomToast("Success.")
                    } else {
                        dialog.dismiss()
                        requireActivity().showCustomToast("Failed to create poll. Please try again.")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.etQuestion -> {
                    if (hasFocus) {
                        if (binding.tvQuestionError.visibility != View.GONE) {
                            binding.tvQuestionError.visibility = View.GONE
                        }
                    } else {
                        validateQuestion()
                    }
                }

                R.id.etOption1 -> {
                    if (hasFocus) {
                        if (binding.tvOption1Error.visibility != View.GONE) {
                            binding.tvOption1Error.visibility = View.GONE
                        }
                    } else {
                        validateOption1()
                    }
                }

                R.id.etOption2 -> {
                    if (hasFocus) {
                        if (binding.tvOption2Error.visibility != View.GONE) {
                            binding.tvOption2Error.visibility = View.GONE
                        }
                    } else {
                        validateOption2()
                    }
                }
            }
        }
    }

    private fun validateQuestion(): Boolean {
        var error: String? = null
        val question = binding.etQuestion.text.toString().trim()
        if (question.isEmpty()) {
            error = "Please enter your question."
        }

        if (error != null) {
            binding.tvQuestionError.visibility = View.VISIBLE
            binding.tvQuestionError.text = error
        } else {
            binding.tvQuestionError.visibility = View.GONE
        }

        return error == null
    }

    private fun validateOption1(): Boolean {
        var error: String? = null
        val option = binding.etOption1.text.toString().trim()
        if (option.isEmpty()) {
            error = "Please enter your question."
        }

        if (error != null) {
            binding.tvOption1Error.visibility = View.VISIBLE
            binding.tvOption1Error.text = error
        } else {
            binding.tvOption1Error.visibility = View.GONE
        }

        return error == null
    }

    private fun validateOption2(): Boolean {
        var error: String? = null
        val option = binding.etOption2.text.toString().trim()
        if (option.isEmpty()) {
            error = "Please enter your question."
        }

        if (error != null) {
            binding.tvOption2Error.visibility = View.VISIBLE
            binding.tvOption2Error.text = error
        } else {
            binding.tvOption2Error.visibility = View.GONE
        }

        return error == null
    }

    private fun validateDeadline(): Boolean {
        var error: String? = null
        val days = binding.daysPicker.value
        val hours = binding.hoursPicker.value
        val minutes = binding.minutesPicker.value

        if (days == 0 && hours == 0 && minutes == 0) {
            error = "Please enter the deadline for this pool."
        }

        if (error != null) {
            binding.tvDeadlineError.visibility = View.VISIBLE
            binding.tvDeadlineError.text = error
        } else {
            binding.tvDeadlineError.visibility = View.GONE
        }

        return error == null
    }
}
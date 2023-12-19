package com.example.letmeknow.ui.main

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentAnswerPollBinding
import com.example.letmeknow.utils.showCustomToast
import com.example.letmeknow.view_model.AnswerPollViewModel
import com.google.android.material.textview.MaterialTextView

class AnswerPollFragment : Fragment() {
    private val args: AnswerPollFragmentArgs by navArgs()

    private var pollId = ""

    private var _binding: FragmentAnswerPollBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AnswerPollViewModel
    private lateinit var dialog: Dialog
    private var selectedButtonId: Int = -1
    private var selectedButtonIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnswerPollBinding.inflate(inflater, container, false)

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

        viewModel = ViewModelProvider(this)[AnswerPollViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pollId = args.id
        dialog.show()
        viewModel.loadData(pollId) { isCompleted ->
            if(isCompleted) {
                inflateData()
                dialog.dismiss()
            } else {
                requireContext().showCustomToast("Failed to load data.")
                findNavController().popBackStack()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            if(selectedButtonId == -1) {
                binding.tvOptionError.visibility = View.VISIBLE
            } else {
                binding.tvOptionError.visibility = View.GONE
                viewModel.recordResponse(pollId, selectedButtonIndex) { successful ->
                    if(successful) {
                        requireContext().showCustomToast("Response recorded.")
                        findNavController().popBackStack()
                    } else {
                        requireContext().showCustomToast("Failed to record response.")
                    }
                }
            }
        }
    }

    private fun inflateData() {
        binding.tvQuestion.text = viewModel.question

        for(item in viewModel.input) {
            if(item.type == "image" && item.imageUrl != "") {
                val imageLayout = LayoutInflater.from(context).inflate(R.layout.layout_poll_image, binding.containerDescription, false)
                val image = imageLayout.findViewById<ImageView>(R.id.ivDescription)

                Glide.with(this)
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(image)

                binding.containerDescription.addView(imageLayout)
            } else if(item.type == "description") {
                val descriptionLayout = LayoutInflater.from(context).inflate(R.layout.layout_poll_description, binding.containerDescription, false)
                val description = descriptionLayout.findViewById<MaterialTextView>(R.id.tvDescription)

                description.text = item.descriptionData

                binding.containerDescription.addView(descriptionLayout)
            }
        }

        for(item in viewModel.options) {
            val optionLayout = LayoutInflater.from(context).inflate(R.layout.layout_poll_option, binding.containerOption, false)
            val radioButton = optionLayout.findViewById<RadioButton>(R.id.btnRadio)

            val index = selectedButtonIndex
            selectedButtonIndex++

            radioButton.text = item
            radioButton.id = View.generateViewId()

            radioButton.setOnClickListener {
                selectedButtonId.takeIf { it != -1 && it != radioButton.id }?.let { lastSelectedId ->
                    val lastSelectedButton = binding.containerOption.findViewById<RadioButton>(lastSelectedId)
                    lastSelectedButton.isChecked = false
                }

                selectedButtonId = radioButton.id
                selectedButtonIndex = index
            }

            binding.containerOption.addView(optionLayout)
        }
    }
}
package com.example.letmeknow.ui.main

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.letmeknow.R
import com.example.letmeknow.databinding.FragmentPollAnalysisBinding
import com.example.letmeknow.utils.IntegerValueFormatter
import com.example.letmeknow.utils.showCustomToast
import com.example.letmeknow.view_model.PollAnalysisViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.textview.MaterialTextView

class PollAnalysisFragment : Fragment() {
    private val args: AnswerPollFragmentArgs by navArgs()

    private var pollId = ""

    private var _binding: FragmentPollAnalysisBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PollAnalysisViewModel
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPollAnalysisBinding.inflate(inflater, container, false)

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

        viewModel = ViewModelProvider(this)[PollAnalysisViewModel::class.java]

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

        for(item in viewModel.optionPair) {
            val optionLayout = LayoutInflater.from(context).inflate(R.layout.layout_analysis_option, binding.containerOption, false)
            val option = optionLayout.findViewById<MaterialTextView>(R.id.tvOption)
            option.text = item.first

            binding.containerOption.addView(optionLayout)
        }

        val barEntries = mutableListOf<BarEntry>()
        viewModel.optionPair.forEachIndexed { index, item ->
            barEntries.add(BarEntry(index.toFloat(), item.second.toFloat()))
        }
        val dataSet = BarDataSet(barEntries, "Poll")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary)

        val barData = BarData(dataSet)

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        val yAxis = binding.barChart.axisLeft
        yAxis.valueFormatter = IntegerValueFormatter()
        yAxis.granularity = 1f

        val yAxis2 = binding.barChart.axisRight
        yAxis2.isEnabled = false

        val description = Description()
        description.text = ""
        binding.barChart.description = description

        binding.barChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text) // X-axis labels
        binding.barChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text) // Left Y-axis labels

        binding.barChart.legend.isEnabled = false
        binding.barChart.setScaleEnabled(false)

        binding.barChart.data = barData

        binding.barChart.invalidate()
    }
}
package com.example.letmeknow.ui.main

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letmeknow.R
import com.example.letmeknow.adapter.HomeRVAdapter
import com.example.letmeknow.databinding.FragmentHomeBinding
import com.example.letmeknow.view_model.HomeViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: Dialog
    private lateinit var viewModel: HomeViewModel
    private val homeAdapter = HomeRVAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

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

        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding.rvPolls.layoutManager = layoutManager
        binding.rvPolls.adapter = homeAdapter

        viewModel.dialogFlag.observe(viewLifecycleOwner) { flag ->
            if(flag) {
                dialog.show()
            }
            else {
                dialog.dismiss()
            }
        }

        viewModel.allPolls.observe(viewLifecycleOwner) { polls ->
            homeAdapter.submitList(polls)
        }

        viewModel.getAllPolls()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
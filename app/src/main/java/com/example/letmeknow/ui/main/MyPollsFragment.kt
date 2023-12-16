package com.example.letmeknow.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.letmeknow.databinding.FragmentMyPollsBinding

class MyPollsFragment : Fragment() {
    private var _binding: FragmentMyPollsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPollsBinding.inflate(inflater, container, false)

        return binding.root
    }
}
package com.shopping.pricecompare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.shopping.pricecompare.R
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchBar()
        setupPopularCategories()
    }

    private fun setupSearchBar() {
        binding.searchBarMain.setOnClickListener {
            val bundle = bundleOf("searchMode" to true, "category" to "전체")
            findNavController().navigate(R.id.action_search_to_productList, bundle)
        }
    }

    private fun setupPopularCategories() {
        val categories = SampleData.categories.drop(1)
        binding.categoryGrid.removeAllViews()
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = false
                textSize = 14f
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.white)
                )
                setTextColor(requireContext().getColor(R.color.primary_color))
                chipStrokeWidth = 2f
                chipStrokeColor = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.primary_color)
                )
            }
            chip.setOnClickListener {
                val bundle = bundleOf("category" to category)
                findNavController().navigate(R.id.action_search_to_productList, bundle)
            }
            binding.categoryGrid.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

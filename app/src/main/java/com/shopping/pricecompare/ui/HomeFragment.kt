package com.shopping.pricecompare.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopping.pricecompare.R
import com.shopping.pricecompare.adapter.ProductAdapter
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.databinding.FragmentHomeBinding
import com.shopping.pricecompare.model.Product

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var specialDealAdapter: ProductAdapter
    private lateinit var lowestPriceAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryButtons()
        setupSpecialDeals()
        setupLowestPriceProducts()
        setupSearchBar()
    }

    private fun setupSearchBar() {
        binding.searchBarHome.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_search)
        }
    }

    private fun setupCategoryButtons() {
        val categories = SampleData.categories.drop(1)
        binding.categoryContainer.removeAllViews()
        categories.forEach { category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category
                isCheckable = false
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
                findNavController().navigate(R.id.action_home_to_productList, bundle)
            }
            binding.categoryContainer.addView(chip)
        }
    }

    private fun navigateToDetail(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("product", product as java.io.Serializable)
        startActivity(intent)
    }

    private fun setupSpecialDeals() {
        specialDealAdapter = ProductAdapter(true) { product -> navigateToDetail(product) }
        binding.rvSpecialDeals.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = specialDealAdapter
        }
        specialDealAdapter.submitList(SampleData.getSpecialDeals().sortedBy { it.totalPrice }.take(6))
    }

    private fun setupLowestPriceProducts() {
        lowestPriceAdapter = ProductAdapter(false) { product -> navigateToDetail(product) }
        binding.rvLowestPrice.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = lowestPriceAdapter
        }
        lowestPriceAdapter.submitList(SampleData.products.sortedBy { it.totalPrice }.take(8))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.shopping.pricecompare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.shopping.pricecompare.R
import com.shopping.pricecompare.adapter.ProductAdapter
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.databinding.FragmentProductListBinding
import com.shopping.pricecompare.model.SortOption
import com.shopping.pricecompare.ui.ProductDetailActivity

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private var currentCategory = "전체"
    private var currentSort = SortOption.LOWEST_TOTAL
    private var currentQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이전 화면에서 전달받은 카테고리
        currentCategory = arguments?.getString("category") ?: "전체"
        val isSearchMode = arguments?.getBoolean("searchMode", false) ?: false

        setupRecyclerView()
        setupCategoryChips()
        setupSearchView(isSearchMode)
        setupSortButton()
        updateProductList()
        updateSortLabel()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            val intent = android.content.Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.removeAllViews()
        SampleData.categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                isChecked = category == currentCategory
                setChipBackgroundColorResource(R.color.primary_light)
                setTextColor(requireContext().getColor(R.color.primary_color))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.primary_color)
            }
            chip.setOnClickListener {
                currentCategory = category
                updateProductList()
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupSearchView(isSearchMode: Boolean) {
        binding.searchViewProducts.isVisible = true
        binding.searchViewProducts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query ?: ""
                updateProductList()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                updateProductList()
                return true
            }
        })
        if (isSearchMode) {
            binding.searchViewProducts.requestFocus()
        }
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener { showSortBottomSheet() }
    }

    private fun showSortBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_sort, null)
        dialog.setContentView(sheetView)

        val sortOptions = SortOption.values()
        sortOptions.forEach { option ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_sort_option, null)
            val tvOption = row.findViewById<android.widget.TextView>(R.id.tv_sort_option)
            val ivCheck = row.findViewById<android.widget.ImageView>(R.id.iv_sort_check)
            tvOption.text = option.label
            ivCheck.isVisible = option == currentSort
            row.setOnClickListener {
                currentSort = option
                updateProductList()
                updateSortLabel()
                dialog.dismiss()
            }
            sheetView.findViewById<android.widget.LinearLayout>(R.id.sort_options_container)
                .addView(row)
        }

        dialog.show()
    }

    private fun updateSortLabel() {
        binding.btnSort.text = "정렬: ${currentSort.label}"
    }

    private fun updateProductList() {
        val baseList = if (currentQuery.isNotBlank()) {
            SampleData.search(currentQuery)
        } else {
            SampleData.getByCategory(currentCategory)
        }

        val sorted = when (currentSort) {
            SortOption.LOWEST_TOTAL  -> baseList.sortedBy { it.totalPrice }
            SortOption.PRICE_LOW     -> baseList.sortedBy { it.price }
            SortOption.PRICE_HIGH    -> baseList.sortedByDescending { it.price }
            SortOption.REVIEW_COUNT  -> baseList.sortedByDescending { it.reviewCount }
            SortOption.RATING        -> baseList.sortedByDescending { it.rating }
        }

        productAdapter.submitList(sorted)
        binding.tvResultCount.text = "총 ${sorted.size}개의 상품"
        binding.tvEmptyState.isVisible = sorted.isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

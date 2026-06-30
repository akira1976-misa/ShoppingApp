package com.shopping.pricecompare.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.shopping.pricecompare.R
import com.shopping.pricecompare.adapter.ProductAdapter
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.databinding.FragmentProductListBinding
import com.shopping.pricecompare.model.FilterState
import com.shopping.pricecompare.model.ShippingType
import com.shopping.pricecompare.model.SortOption
import com.shopping.pricecompare.viewmodel.SearchViewModel

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private var currentCategory = "전체"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentCategory = arguments?.getString("category") ?: "전체"
        val searchMode = arguments?.getBoolean("searchMode", false) ?: false

        setupRecyclerView()
        setupCategoryChips()
        setupSearch(searchMode)
        setupFilterButton()
        setupSortButton()
        setupPaginationButtons()
        observeViewModel()

        if (!searchMode) viewModel.loadByCategory(currentCategory)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(isHorizontal = false) { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("product", product as java.io.Serializable)
            startActivity(intent)
        }
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = productAdapter
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.removeAllViews()
        SampleData.categories.forEach { cat ->
            val chip = makeChip(cat, cat == currentCategory)
            chip.setOnClickListener {
                currentCategory = cat
                binding.etSearch.setText("")
                setupCategoryChips()
                viewModel.loadByCategory(cat)
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupSearch(searchMode: Boolean) {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                binding.btnSearchClear.isVisible = q.isNotBlank()
                if (q.isBlank()) viewModel.loadByCategory(currentCategory)
                else viewModel.searchDebounced(q)
            }
        })
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(binding.etSearch.text.toString())
                hideKeyboard(); true
            } else false
        }
        binding.btnSearchClear.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.loadByCategory(currentCategory)
        }
        if (searchMode) {
            binding.etSearch.requestFocus()
            val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setupFilterButton() { binding.btnFilter.setOnClickListener { showFilterSheet() } }

    private fun showFilterSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheet = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_filter, null)
        dialog.setContentView(sheet)
        val current = viewModel.filterState.value ?: FilterState()
        val brands = viewModel.availableBrands.value ?: emptyList()

        val shipGroup = sheet.findViewById<ChipGroup>(R.id.chip_group_shipping)
        ShippingType.values().forEach { type ->
            val chip = makeChip(type.label, current.shippingType == type)
            chip.tag = type
            shipGroup.addView(chip)
        }

        val priceGroup = sheet.findViewById<ChipGroup>(R.id.chip_group_price)
        FilterState.PRICE_PRESETS.forEach { (label, min, max) ->
            val chip = makeChip(label, current.minPrice == min && current.maxPrice == max)
            chip.tag = Pair(min, max)
            priceGroup.addView(chip)
        }

        sheet.findViewById<TextView>(R.id.btn_price_apply).setOnClickListener {
            for (i in 0 until priceGroup.childCount) (priceGroup.getChildAt(i) as? Chip)?.isChecked = false
        }

        val brandGroup = sheet.findViewById<ChipGroup>(R.id.chip_group_brand)
        val noBrandsTv = sheet.findViewById<TextView>(R.id.tv_no_brands)
        if (brands.isEmpty()) {
            noBrandsTv.isVisible = true
        } else {
            noBrandsTv.isVisible = false
            brands.forEach { brand -> brandGroup.addView(makeChip(brand, brand in current.selectedBrands)) }
        }

        sheet.findViewById<TextView>(R.id.btn_filter_reset).setOnClickListener {
            viewModel.clearFilter(); dialog.dismiss()
        }

        sheet.findViewById<TextView>(R.id.btn_filter_apply).setOnClickListener {
            var selectedShipping = ShippingType.ALL
            for (i in 0 until shipGroup.childCount) {
                val c = shipGroup.getChildAt(i) as? Chip ?: continue
                if (c.isChecked) { selectedShipping = c.tag as ShippingType; break }
            }
            var selectedMin = 0; var selectedMax = Int.MAX_VALUE
            for (i in 0 until priceGroup.childCount) {
                val c = priceGroup.getChildAt(i) as? Chip ?: continue
                if (c.isChecked) {
                    val pair = c.tag as? Pair<*, *>
                    selectedMin = (pair?.first as? Int) ?: 0
                    selectedMax = (pair?.second as? Int) ?: Int.MAX_VALUE
                    break
                }
            }
            val minTxt = sheet.findViewById<EditText>(R.id.et_min_price).text.toString()
            val maxTxt = sheet.findViewById<EditText>(R.id.et_max_price).text.toString()
            if (minTxt.isNotBlank() || maxTxt.isNotBlank()) {
                selectedMin = minTxt.toIntOrNull() ?: 0
                selectedMax = maxTxt.toIntOrNull() ?: Int.MAX_VALUE
            }
            val selectedBrands = mutableSetOf<String>()
            for (i in 0 until brandGroup.childCount) {
                val c = brandGroup.getChildAt(i) as? Chip ?: continue
                if (c.isChecked) selectedBrands.add(c.text.toString())
            }
            viewModel.applyFilter(FilterState(selectedShipping, selectedMin, selectedMax, selectedBrands))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val sv = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_sort, null)
            dialog.setContentView(sv)
            val cont = sv.findViewById<LinearLayout>(R.id.sort_options_container)
            SortOption.values().forEach { opt ->
                val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_sort_option, null)
                row.findViewById<TextView>(R.id.tv_sort_option).text = opt.label
                row.findViewById<android.widget.ImageView>(R.id.iv_sort_check).isVisible = opt == viewModel.currentSort
                row.setOnClickListener {
                    viewModel.changeSort(opt)
                    binding.tvSortLabel.text = opt.label
                    dialog.dismiss()
                }
                cont.addView(row)
            }
            dialog.show()
        }
    }

    private fun setupPaginationButtons() {
        binding.btnPrevPage.setOnClickListener { viewModel.prevPage(); binding.rvProducts.scrollToPosition(0) }
        binding.btnNextPage.setOnClickListener { viewModel.nextPage(); binding.rvProducts.scrollToPosition(0) }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressList.isVisible = loading
            binding.rvProducts.isVisible = !loading
        }
        viewModel.products.observe(viewLifecycleOwner) { list ->
            productAdapter.submitList(list)
            binding.tvEmptyState.isVisible = list.isEmpty() && viewModel.isLoading.value == false
        }
        viewModel.resultCount.observe(viewLifecycleOwner) { binding.tvResultCount.text = it }
        viewModel.filterState.observe(viewLifecycleOwner) { filter ->
            val count = filter.activeCount
            binding.tvFilterBadge.isVisible = count > 0
            binding.tvFilterBadge.text = if (count > 0) "$count" else ""
            binding.tvFilterLabel.text = if (count > 0) "필터 $count" else "필터"
        }
        viewModel.paging.observe(viewLifecycleOwner) { paging ->
            val show = paging.totalPages > 1
            binding.paginationBar.isVisible = show
            if (show) {
                binding.tvPageInfo.text = "${paging.currentPage} / ${paging.totalPages} 페이지"
                binding.btnPrevPage.alpha = if (paging.hasPrev) 1f else 0.4f
                binding.btnPrevPage.isEnabled = paging.hasPrev
                binding.btnNextPage.alpha = if (paging.hasNext) 1f else 0.4f
                binding.btnNextPage.isEnabled = paging.hasNext
            }
        }
    }

    private fun makeChip(text: String, selected: Boolean): Chip {
        return Chip(requireContext()).apply {
            this.text = text; isCheckable = true; isChecked = selected; chipStrokeWidth = 0f
            setChipBackgroundColorResource(if (selected) R.color.primary_color else R.color.chip_unselected_bg)
            setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.white else R.color.primary_color))
        }
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

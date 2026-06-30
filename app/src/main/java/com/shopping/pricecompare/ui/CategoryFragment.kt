package com.shopping.pricecompare.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shopping.pricecompare.R
import com.shopping.pricecompare.data.CategoryData
import com.shopping.pricecompare.data.MidCategoryItem
import com.shopping.pricecompare.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private var _b: FragmentCategoryBinding? = null
    private val b get() = _b!!

    private var mainList: List<String> = emptyList()
    private var midList: List<MidCategoryItem> = emptyList()

    private var selectedMainIndex = 0
    private var selectedMidIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentCategoryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainList = CategoryData.mainNames(requireContext())
        buildMainCategories()
    }

    private fun buildMainCategories() {
        b.mainCategoryContainer.removeAllViews()
        mainList.forEachIndexed { index, name ->
            val tv = makeItem(name, index == selectedMainIndex, R.color.bg_main, R.color.white, true)
            tv.setOnClickListener {
                selectedMainIndex = index
                selectedMidIndex = 0
                buildMainCategories()
            }
            b.mainCategoryContainer.addView(tv)
            b.mainCategoryContainer.addView(makeDivider())
        }
        if (mainList.isNotEmpty()) {
            midList = CategoryData.getMids(requireContext(), mainList[selectedMainIndex])
            buildMidCategories()
        }
    }

    private fun buildMidCategories() {
        b.midCategoryContainer.removeAllViews()
        b.subCategoryContainer.removeAllViews()
        midList.forEachIndexed { index, mid ->
            val tv = makeItem(mid.name, index == selectedMidIndex, R.color.primary_light, R.color.bg_main, false)
            tv.setOnClickListener {
                selectedMidIndex = index
                buildMidCategories()
            }
            b.midCategoryContainer.addView(tv)
            b.midCategoryContainer.addView(makeDivider())
        }
        if (midList.isNotEmpty()) buildSubCategories(midList[selectedMidIndex])
    }

    private fun buildSubCategories(mid: MidCategoryItem) {
        b.subCategoryContainer.removeAllViews()

        val allTv = makeSubItem("▶ ${mid.name} 전체")
        allTv.setTextColor(requireContext().getColor(R.color.primary_color))
        allTv.setTypeface(null, Typeface.BOLD)
        allTv.setOnClickListener {
            findNavController().navigate(R.id.action_category_to_productList, bundleOf("category" to mid.name))
        }
        b.subCategoryContainer.addView(allTv)
        b.subCategoryContainer.addView(makeDivider())

        if (mid.subs.isEmpty()) {
            val empty = makeSubItem("하위 카테고리가 없습니다")
            empty.setTextColor(requireContext().getColor(R.color.text_hint))
            b.subCategoryContainer.addView(empty)
            return
        }
        mid.subs.forEach { subName ->
            val tv = makeSubItem(subName)
            tv.setOnClickListener {
                findNavController().navigate(R.id.action_category_to_productList, bundleOf("category" to subName))
            }
            b.subCategoryContainer.addView(tv)
            b.subCategoryContainer.addView(makeDivider())
        }
    }

    private fun makeItem(text: String, selected: Boolean, bgSelected: Int, bgUnselected: Int, bold: Boolean): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setPadding(14, 20, 14, 20)
            setTypeface(null, if (selected && bold) Typeface.BOLD else Typeface.NORMAL)
            setBackgroundColor(requireContext().getColor(if (selected) bgSelected else bgUnselected))
            setTextColor(requireContext().getColor(if (selected) R.color.primary_color else R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            isClickable = true; isFocusable = true
        }
    }

    private fun makeSubItem(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setPadding(20, 18, 16, 18)
            setTextColor(requireContext().getColor(R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            isClickable = true; isFocusable = true
        }
    }

    private fun makeDivider(): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(requireContext().getColor(R.color.divider_color))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

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
import com.shopping.pricecompare.data.MidCategory
import com.shopping.pricecompare.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private var _b: FragmentCategoryBinding? = null
    private val b get() = _b!!

    private var selectedMainIndex = 0
    private var selectedMidIndex  = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentCategoryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildMainCategories()
    }

    // ── 대분류 ────────────────────────────────────────────────────────────
    private fun buildMainCategories() {
        b.mainCategoryContainer.removeAllViews()
        CategoryData.tree.forEachIndexed { index, main ->
            val tv = makeItem(
                text     = main.name,
                selected = index == selectedMainIndex,
                bgSelected   = R.color.bg_main,
                bgUnselected = R.color.white,
                bold     = true
            )
            tv.setOnClickListener {
                selectedMainIndex = index
                selectedMidIndex  = 0
                buildMainCategories()
            }
            b.mainCategoryContainer.addView(tv)
            b.mainCategoryContainer.addView(makeDivider())
        }
        // 선택된 대분류의 중분류 표시
        if (CategoryData.tree.isNotEmpty()) {
            buildMidCategories(CategoryData.tree[selectedMainIndex].mids)
        }
    }

    // ── 중분류 ────────────────────────────────────────────────────────────
    private fun buildMidCategories(mids: List<MidCategory>) {
        b.midCategoryContainer.removeAllViews()
        b.subCategoryContainer.removeAllViews()

        mids.forEachIndexed { index, mid ->
            val tv = makeItem(
                text     = mid.name,
                selected = index == selectedMidIndex,
                bgSelected   = R.color.primary_light,
                bgUnselected = R.color.bg_main,
                bold     = false
            )
            tv.setOnClickListener {
                selectedMidIndex = index
                buildMidCategories(mids)
            }
            b.midCategoryContainer.addView(tv)
            b.midCategoryContainer.addView(makeDivider())
        }
        if (mids.isNotEmpty()) {
            buildSubCategories(mids[selectedMidIndex])
        }
    }

    // ── 소분류 ────────────────────────────────────────────────────────────
    private fun buildSubCategories(mid: MidCategory) {
        b.subCategoryContainer.removeAllViews()

        // 중분류 전체보기
        val allTv = makeSubItem("▶ ${mid.name} 전체")
        allTv.setTextColor(requireContext().getColor(R.color.primary_color))
        allTv.setTypeface(null, Typeface.BOLD)
        allTv.setOnClickListener {
            findNavController().navigate(
                R.id.action_category_to_productList,
                bundleOf("category" to mid.name)
            )
        }
        b.subCategoryContainer.addView(allTv)
        b.subCategoryContainer.addView(makeDivider())

        // 소분류 항목들
        mid.subs.forEach { sub ->
            val tv = makeSubItem(sub.name)
            tv.setOnClickListener {
                findNavController().navigate(
                    R.id.action_category_to_productList,
                    bundleOf("category" to sub.name)
                )
            }
            b.subCategoryContainer.addView(tv)
            b.subCategoryContainer.addView(makeDivider())
        }
    }

    // ── 뷰 헬퍼 ──────────────────────────────────────────────────────────
    private fun makeItem(
        text: String,
        selected: Boolean,
        bgSelected: Int,
        bgUnselected: Int,
        bold: Boolean
    ): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize  = 13f
            setPadding(16, 22, 16, 22)
            setTypeface(null, if (selected && bold) Typeface.BOLD else Typeface.NORMAL)
            setBackgroundColor(
                requireContext().getColor(if (selected) bgSelected else bgUnselected))
            setTextColor(
                requireContext().getColor(
                    if (selected) R.color.primary_color else R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            isClickable = true
            isFocusable = true
        }
    }

    private fun makeSubItem(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize  = 13f
            setPadding(20, 20, 16, 20)
            setTextColor(requireContext().getColor(R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            isClickable = true
            isFocusable = true
        }
    }

    private fun makeDivider(): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(requireContext().getColor(R.color.divider_color))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView(); _b = null
    }
}

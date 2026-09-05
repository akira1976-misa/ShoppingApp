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
    private var selectedMidIndex  = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentCategoryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainList = CategoryData.mainNames(requireContext())

        // 우측 상단 검색(전체보기) 버튼 클릭
        // → 현재 선택된 대분류 또는 중분류 기준으로 상품 전체 표시
        b.btnCategorySearch.setOnClickListener {
            val searchCategory = when {
                // 중분류가 선택되어 있으면 중분류 기준으로 검색
                midList.isNotEmpty() ->
                    midList.getOrNull(selectedMidIndex)?.name
                        ?: mainList.getOrNull(selectedMainIndex) ?: "전체"
                // 대분류만 선택된 경우 대분류 기준으로 검색
                mainList.isNotEmpty() ->
                    mainList.getOrNull(selectedMainIndex) ?: "전체"
                else -> "전체"
            }
            findNavController().navigate(
                R.id.action_category_to_productList,
                bundleOf("category" to searchCategory, "searchMode" to false)
            )
        }

        buildMainCategories()
    }

    // ── 대분류 목록 ────────────────────────────────────────────────────────
    private fun buildMainCategories() {
        b.mainCategoryContainer.removeAllViews()

        mainList.forEachIndexed { index, name ->
            val tv = makeItem(
                text        = name,
                selected    = index == selectedMainIndex,
                bgSelected  = R.color.bg_main,
                bgNormal    = R.color.white,
                bold        = true
            )
            tv.setOnClickListener {
                selectedMainIndex = index
                selectedMidIndex  = 0
                buildMainCategories() // 선택 상태 갱신
            }
            b.mainCategoryContainer.addView(tv)
            b.mainCategoryContainer.addView(makeDivider())
        }

        // 선택된 대분류의 중분류 표시
        if (mainList.isNotEmpty()) {
            midList = CategoryData.getMids(requireContext(), mainList[selectedMainIndex])
            buildMidCategories()
        }

        updatePathLabel()
    }

    // ── 중분류 목록 ────────────────────────────────────────────────────────
    private fun buildMidCategories() {
        b.midCategoryContainer.removeAllViews()
        b.subCategoryContainer.removeAllViews()

        midList.forEachIndexed { index, mid ->
            val tv = makeItem(
                text        = mid.name,
                selected    = index == selectedMidIndex,
                bgSelected  = R.color.primary_light,
                bgNormal    = R.color.bg_main,
                bold        = false
            )
            tv.setOnClickListener {
                selectedMidIndex = index
                buildMidCategories() // 선택 상태 갱신
            }
            b.midCategoryContainer.addView(tv)
            b.midCategoryContainer.addView(makeDivider())
        }

        if (midList.isNotEmpty()) {
            buildSubCategories(midList[selectedMidIndex])
        }

        updatePathLabel()
    }

    // ── 소분류 목록 ────────────────────────────────────────────────────────
    // 소분류 탭 → 자동으로 해당 상품 목록으로 이동 (기존 동작 유지)
    private fun buildSubCategories(mid: MidCategoryItem) {
        b.subCategoryContainer.removeAllViews()

        if (mid.subs.isEmpty()) {
            val empty = makeSubItem("항목이 없습니다")
            empty.setTextColor(requireContext().getColor(R.color.text_hint))
            b.subCategoryContainer.addView(empty)
            return
        }

        mid.subs.forEach { subName ->
            val tv = makeSubItem(subName)
            tv.setOnClickListener {
                // 소분류 탭 → 바로 상품 목록 화면으로 이동 (기존 동작 유지)
                findNavController().navigate(
                    R.id.action_category_to_productList,
                    bundleOf("category" to subName, "searchMode" to false)
                )
            }
            b.subCategoryContainer.addView(tv)
            b.subCategoryContainer.addView(makeDivider())
        }
    }

    // ── 경로 라벨 업데이트 ─────────────────────────────────────────────────
    // 예: "전자기기 > TV·영상  —  🔍 전체보기를 누르면 이 카테고리 상품을 전부 봅니다"
    private fun updatePathLabel() {
        val mainName = mainList.getOrNull(selectedMainIndex) ?: ""
        val midName  = midList.getOrNull(selectedMidIndex)?.name ?: ""

        val path = when {
            midName.isNotEmpty() -> "$mainName  >  $midName"
            mainName.isNotEmpty() -> mainName
            else -> ""
        }
        b.tvCategoryPath.text = if (path.isNotEmpty())
            "📂 $path  —  상단 [전체보기]로 이 카테고리 상품을 전부 볼 수 있습니다"
        else
            "대분류 선택 후 상단 [전체보기] 버튼을 눌러보세요"
    }

    // ── 뷰 헬퍼 ──────────────────────────────────────────────────────────
    private fun makeItem(
        text: String, selected: Boolean,
        bgSelected: Int, bgNormal: Int, bold: Boolean
    ): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize  = 13f
            setPadding(14, 20, 14, 20)
            setTypeface(null, if (selected && bold) Typeface.BOLD else Typeface.NORMAL)
            setBackgroundColor(requireContext().getColor(if (selected) bgSelected else bgNormal))
            setTextColor(requireContext().getColor(
                if (selected) R.color.primary_color else R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            isClickable = true; isFocusable = true
        }
    }

    private fun makeSubItem(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize  = 13f
            setPadding(20, 18, 16, 18)
            setTextColor(requireContext().getColor(R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            isClickable = true; isFocusable = true
        }
    }

    private fun makeDivider(): View {
        return View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(requireContext().getColor(R.color.divider_color))
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

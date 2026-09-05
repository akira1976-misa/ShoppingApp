package com.shopping.pricecompare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shopping.pricecompare.R
import com.shopping.pricecompare.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _b: FragmentSearchBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentSearchBinding.inflate(inflater, container, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.searchBarMain.setOnClickListener {
            findNavController().navigate(R.id.action_search_to_productList,
                bundleOf("searchMode" to true, "category" to "전체"))
        }
        b.categoryGrid.removeAllViews()
        val hint = android.widget.TextView(requireContext()).apply {
            text = "세부 카테고리 탐색은 하단 [카테고리] 탭을 이용해 주세요 →"
            textSize = 13f
            setPadding(0, 24, 0, 0)
            setTextColor(requireContext().getColor(R.color.text_secondary))
        }
        b.categoryGrid.addView(hint)
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

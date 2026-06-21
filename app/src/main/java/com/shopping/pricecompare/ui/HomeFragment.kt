package com.shopping.pricecompare.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shopping.pricecompare.R
import com.shopping.pricecompare.adapter.ProductAdapter
import com.shopping.pricecompare.databinding.FragmentHomeBinding
import com.shopping.pricecompare.model.Product
import com.shopping.pricecompare.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 검색바 클릭 → 검색 모드로 상품목록 화면 이동 (검색창 자동 포커스)
        b.searchBarHome.setOnClickListener {
            findNavController().navigate(
                R.id.action_home_to_productList,
                bundleOf("category" to "전체", "searchMode" to true)
            )
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        vm.isLoading.observe(viewLifecycleOwner) { b.progressHome.isVisible = it }

        vm.sections.observe(viewLifecycleOwner) { sections ->
            b.hotDealsContainer.removeAllViews()
            sections.forEach { section ->
                val sv = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_section, b.hotDealsContainer, false)

                sv.findViewById<TextView>(R.id.tv_section_title).text = section.title

                // "전체보기" → 해당 카테고리 상품목록으로 이동 (검색 화면 아님!)
                sv.findViewById<TextView>(R.id.tv_section_more).setOnClickListener {
                    findNavController().navigate(
                        R.id.action_home_to_productList,
                        bundleOf("category" to section.category, "searchMode" to false)
                    )
                }

                val adapter = ProductAdapter(isHorizontal = true) { p -> goToDetail(p) }
                val rv = sv.findViewById<RecyclerView>(R.id.rv_section_products)
                rv.layoutManager = LinearLayoutManager(
                    requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rv.adapter = adapter
                rv.isNestedScrollingEnabled = false
                adapter.submitList(section.products)

                b.hotDealsContainer.addView(sv)
            }
        }
    }

    private fun goToDetail(p: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("product", p as java.io.Serializable)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView(); _b = null
    }
}

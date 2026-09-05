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
import com.shopping.pricecompare.ui.PrivacyPolicyActivity
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

        // 검색바 → 상품 목록(검색 모드)
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

            // ── 구글 플레이 정책 필수: 제휴마케팅 고지 배너 ────────────────
            val affiliateBanner = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_affiliate_notice, b.hotDealsContainer, false)
            affiliateBanner.setOnClickListener {
                // 개인정보처리방침 표시
                startActivity(
                    Intent(requireContext(), PrivacyPolicyActivity::class.java)
                )
            }
            b.hotDealsContainer.addView(affiliateBanner)

            sections.forEach { section ->
                val sv = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_section, b.hotDealsContainer, false)

                sv.findViewById<TextView>(R.id.tv_section_title).text = section.title
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
        startActivity(
            Intent(requireContext(), ProductDetailActivity::class.java)
                .putExtra("product", p as java.io.Serializable)
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

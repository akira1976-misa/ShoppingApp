package com.shopping.pricecompare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.model.Product
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    data class CategorySection(
        val category: String,
        val title: String,
        val products: List<Product>
    )

    private val _sections  = MutableLiveData<List<CategorySection>>(emptyList())
    val sections: LiveData<List<CategorySection>> = _sections

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init { loadSections() }

    fun loadSections() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = SampleData.categories.drop(1).map { cat ->
                val items    = SampleData.getHotDealsByCategory(cat, 5)
                val hasDeals = items.any { it.isSpecialDeal }
                CategorySection(cat,
                    if (hasDeals) "🔥 $cat 핫딜" else "💰 $cat 최저가",
                    items)
            }.filter { it.products.isNotEmpty() }
            _sections.value  = result
            _isLoading.value = false
        }
    }
}

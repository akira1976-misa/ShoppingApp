package com.shopping.pricecompare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.pricecompare.api.NaverShoppingService
import com.shopping.pricecompare.data.SampleData
import com.shopping.pricecompare.model.FilterState
import com.shopping.pricecompare.model.Product
import com.shopping.pricecompare.model.ShippingType
import com.shopping.pricecompare.model.SortOption
import com.shopping.pricecompare.repository.ShoppingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    companion object { const val PAGE_SIZE = 120 }

    private val _allProducts = mutableListOf<Product>()
    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _resultCount = MutableLiveData("총 0개")
    val resultCount: LiveData<String> = _resultCount

    data class PagingState(val currentPage: Int = 1, val totalPages: Int = 1, val hasPrev: Boolean = false, val hasNext: Boolean = false)
    private val _paging = MutableLiveData(PagingState())
    val paging: LiveData<PagingState> = _paging

    private val _filterState = MutableLiveData(FilterState())
    val filterState: LiveData<FilterState> = _filterState
    private val _availableBrands = MutableLiveData<List<String>>(emptyList())
    val availableBrands: LiveData<List<String>> = _availableBrands

    var currentSort = SortOption.LOWEST_TOTAL
    private var currentCategory = "전체"
    private var currentQuery = ""
    private var savedCurrentPage = 1
    private var savedTotalPages = 1
    private var searchJob: Job? = null

    fun searchDebounced(query: String) {
        searchJob?.cancel()
        currentQuery = query
        if (query.isBlank()) return
        searchJob = viewModelScope.launch { delay(500L); loadData(query = query, page = 1) }
    }
    fun search(query: String, page: Int = 1) {
        currentQuery = query
        if (query.isBlank()) return
        viewModelScope.launch { loadData(query = query, page = page) }
    }
    fun loadByCategory(category: String, page: Int = 1) {
        currentCategory = category; currentQuery = ""
        viewModelScope.launch { loadData(category = category, page = page) }
    }
    fun nextPage() {
        val cur = _paging.value ?: return
        if (!cur.hasNext) return
        if (currentQuery.isNotBlank()) search(currentQuery, cur.currentPage + 1)
        else loadByCategory(currentCategory, cur.currentPage + 1)
    }
    fun prevPage() {
        val cur = _paging.value ?: return
        if (!cur.hasPrev) return
        if (currentQuery.isNotBlank()) search(currentQuery, cur.currentPage - 1)
        else loadByCategory(currentCategory, cur.currentPage - 1)
    }
    fun applyFilter(filter: FilterState) { _filterState.value = filter; applyAndUpdate() }
    fun clearFilter() { _filterState.value = FilterState(); applyAndUpdate() }
    fun changeSort(sort: SortOption) { currentSort = sort; applyAndUpdate() }

    private suspend fun loadData(query: String = "", category: String = "", page: Int = 1) {
        _isLoading.value = true
        try {
            val searchQuery = when {
                query.isNotBlank() -> query
                category == "전체" -> "인기상품"
                else -> category
            }
            val result = NaverShoppingService.searchPaged(searchQuery, page, PAGE_SIZE)
            val products = if (result.items.isNotEmpty()) {
                ShoppingRepository.convertItems(result.items).let { list ->
                    if (category.isNotBlank() && category != "전체") list.map { it.copy(category = category) } else list
                }
            } else {
                if (query.isNotBlank()) SampleData.search(query) else SampleData.getByCategory(category)
            }
            _allProducts.clear(); _allProducts.addAll(products)
            savedCurrentPage = result.currentPage; savedTotalPages = result.totalPages
            extractBrands(products)
            applyAndUpdate()
        } catch (e: Exception) {
            val fallback = if (query.isNotBlank()) SampleData.search(query) else SampleData.getByCategory(category)
            _allProducts.clear(); _allProducts.addAll(fallback)
            savedCurrentPage = 1; savedTotalPages = 1
            applyAndUpdate()
        } finally { _isLoading.value = false }
    }

    private fun applyAndUpdate() {
        val f = _filterState.value ?: FilterState()
        var list = _allProducts.toList()
        list = when (f.shippingType) {
            ShippingType.FREE -> list.filter { it.isFreeShipping }
            ShippingType.DOMESTIC -> list.filter { !isOverseas(it) }
            ShippingType.OVERSEAS -> list.filter { isOverseas(it) }
            ShippingType.ALL -> list
        }
        if (f.minPrice > 0 || f.maxPrice < Int.MAX_VALUE) list = list.filter { it.lowestTotalPrice in f.minPrice..f.maxPrice }
        if (f.selectedBrands.isNotEmpty()) list = list.filter { p -> f.selectedBrands.any { b -> p.name.contains(b, true) } }
        list = when (currentSort) {
            SortOption.LOWEST_TOTAL -> list.sortedBy { it.lowestTotalPrice }
            SortOption.PRICE_LOW -> list.sortedBy { it.lowestPrice }
            SortOption.PRICE_HIGH -> list.sortedByDescending { it.lowestPrice }
            SortOption.REVIEW_COUNT -> list.sortedByDescending { it.reviewCount }
            SortOption.RATING -> list.sortedByDescending { it.rating }
        }
        _products.value = list
        _resultCount.value = "총 ${list.size}개 (${savedCurrentPage}/${savedTotalPages}페이지)"
        _paging.value = PagingState(savedCurrentPage, savedTotalPages, savedCurrentPage > 1, savedCurrentPage < savedTotalPages)
    }

    private fun isOverseas(p: Product): Boolean {
        val kw = listOf("해외","직구","global","amazon","aliexpress")
        return p.sellers.any { s -> kw.any { s.shopName.contains(it, true) } }
    }
    private fun extractBrands(products: List<Product>) {
        val known = listOf("삼성","애플","LG","소니","나이키","아디다스","뉴발란스","유니클로","노스페이스","다이슨","쿠쿠","테팔","설화수","라네즈","닥터자르트","가민","필립스","보스")
        _availableBrands.value = known.filter { brand -> products.any { it.name.contains(brand, true) } }
    }
}

package com.shopping.pricecompare.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

    // ── 상태 열거형으로 빈화면 / 오류화면 명확히 구분 ─────────────────────
    sealed class UiState {
        object Idle    : UiState()
        object Loading : UiState()
        object Empty   : UiState()              // 검색 결과 없음
        class  Error(val msg: String) : UiState() // 네트워크/API 오류
        class  Success(val items: List<Product>) : UiState()
    }

    private val _uiState     = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    // products 는 Success 상태에서만 사용 (기존 코드와 호환)
    private val _products    = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _resultCount = MutableLiveData("총 0개")
    val resultCount: LiveData<String> = _resultCount

    // 로딩 여부 - MediatorLiveData로 uiState에서 파생
    private val _isLoading = MediatorLiveData<Boolean>().also { ld ->
        ld.value = false
        ld.addSource(_uiState) { ld.value = it is UiState.Loading }
    }
    val isLoading: LiveData<Boolean> = _isLoading

    data class PagingState(
        val currentPage: Int = 1, val totalPages: Int = 1,
        val hasPrev: Boolean = false, val hasNext: Boolean = false
    )
    private val _paging          = MutableLiveData(PagingState())
    val paging: LiveData<PagingState> = _paging

    private val _filterState     = MutableLiveData(FilterState())
    val filterState: LiveData<FilterState> = _filterState

    private val _availableBrands = MutableLiveData<List<String>>(emptyList())
    val availableBrands: LiveData<List<String>> = _availableBrands

    var currentSort              = SortOption.LOWEST_TOTAL
    private var currentCategory  = "전체"
    private var currentQuery     = ""
    private var savedCurrentPage = 1
    private var savedTotalPages  = 1

    // allProducts: private으로만 변경 가능하게 (외부 직접 변경 차단)
    private val allProducts      = mutableListOf<Product>()

    private var searchJob: Job? = null

    // ─── 검색 ───────────────────────────────────────────────────────────────
    fun searchDebounced(query: String) {
        searchJob?.cancel()
        currentQuery = query
        if (query.isBlank()) {
            clearResults()
            return
        }
        searchJob = viewModelScope.launch {
            // API 없으면 로컬 검색이라 즉시, 있으면 500ms 디바운스
            val hasApi = com.shopping.pricecompare.BuildConfig.NAVER_CLIENT_ID.isNotEmpty()
            if (hasApi) delay(500L)
            loadData(query = query, page = 1)
        }
    }

    fun search(query: String, page: Int = 1) {
        currentQuery = query
        if (query.isBlank()) { clearResults(); return }
        viewModelScope.launch { loadData(query = query, page = page) }
    }

    fun loadByCategory(category: String, page: Int = 1) {
        currentCategory = category
        currentQuery    = ""
        viewModelScope.launch { loadData(category = category, page = page) }
    }

    // ─── 페이지 ─────────────────────────────────────────────────────────────
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

    // ─── 필터 ───────────────────────────────────────────────────────────────
    fun applyFilter(filter: FilterState) { _filterState.value = filter; applyAndUpdate() }
    fun clearFilter()                    { _filterState.value = FilterState(); applyAndUpdate() }
    fun changeSort(sort: SortOption)     { currentSort = sort; applyAndUpdate() }

    // ─── 내부 ───────────────────────────────────────────────────────────────
    private suspend fun loadData(query: String = "", category: String = "", page: Int = 1) {
        _uiState.value = UiState.Loading

        try {
            val searchQuery = when {
                query.isNotBlank()  -> query
                category == "전체" -> "인기상품"
                else               -> category
            }
            val result   = NaverShoppingService.searchPaged(searchQuery, page, PAGE_SIZE)
            val products = if (result.items.isNotEmpty()) {
                ShoppingRepository.convertItems(result.items).let { list ->
                    if (category.isNotBlank() && category != "전체")
                        list.map { it.copy(category = category) } else list
                }
            } else {
                // API 키 없거나 결과 없으면 로컬 폴백
                if (query.isNotBlank()) SampleData.search(query)
                else SampleData.getByCategory(category)
            }
            allProducts.clear(); allProducts.addAll(products)
            savedCurrentPage = result.currentPage
            savedTotalPages  = result.totalPages
            extractBrands(products)
            applyAndUpdate()

        } catch (e: Exception) {
            // 네트워크/API 오류 → 로컬 폴백 + 오류 상태
            val fallback = if (query.isNotBlank()) SampleData.search(query)
                           else SampleData.getByCategory(category)
            allProducts.clear(); allProducts.addAll(fallback)
            savedCurrentPage = 1; savedTotalPages = 1

            if (fallback.isNotEmpty()) {
                applyAndUpdate()  // 로컬 데이터라도 보여줌
            } else {
                _uiState.value = UiState.Error("인터넷 연결을 확인해 주세요.")
            }
        }
    }

    private fun applyAndUpdate() {
        val f    = _filterState.value ?: FilterState()
        var list = allProducts.toList()

        // 배송 필터
        list = when (f.shippingType) {
            ShippingType.FREE     -> list.filter { it.isFreeShipping }
            ShippingType.DOMESTIC -> list.filter { !isOverseas(it) }
            ShippingType.OVERSEAS -> list.filter { isOverseas(it) }
            ShippingType.ALL      -> list
        }
        // 가격 필터
        if (f.minPrice > 0 || f.maxPrice < Int.MAX_VALUE)
            list = list.filter { it.lowestTotalPrice in f.minPrice..f.maxPrice }
        // 브랜드 필터
        if (f.selectedBrands.isNotEmpty())
            list = list.filter { p -> f.selectedBrands.any { b -> p.name.contains(b, true) } }
        // 정렬
        list = when (currentSort) {
            SortOption.LOWEST_TOTAL -> list.sortedBy { it.lowestTotalPrice }
            SortOption.PRICE_LOW    -> list.sortedBy { it.lowestPrice }
            SortOption.PRICE_HIGH   -> list.sortedByDescending { it.lowestPrice }
            SortOption.REVIEW_COUNT -> list.sortedByDescending { it.reviewCount }
            SortOption.RATING       -> list.sortedByDescending { it.rating }
        }

        _products.value    = list
        _resultCount.value = "총 ${list.size}개 (${savedCurrentPage}/${savedTotalPages}페이지)"
        _paging.value      = PagingState(savedCurrentPage, savedTotalPages,
            savedCurrentPage > 1, savedCurrentPage < savedTotalPages)

        // UiState 업데이트
        _uiState.value = if (list.isEmpty()) UiState.Empty else UiState.Success(list)
    }

    private fun clearResults() {
        allProducts.clear()
        _products.value    = emptyList()
        _resultCount.value = "총 0개"
        _paging.value      = PagingState()
        _uiState.value     = UiState.Idle
    }

    private fun isOverseas(p: Product): Boolean {
        val kw = listOf("해외","직구","global","amazon","aliexpress")
        return p.sellers.any { s -> kw.any { s.shopName.contains(it, true) } }
    }

    private fun extractBrands(products: List<Product>) {
        val known = listOf("삼성","애플","LG","소니","나이키","아디다스","뉴발란스",
            "유니클로","노스페이스","다이슨","쿠쿠","테팔","설화수","라네즈","닥터자르트","가민","필립스","보스")
        _availableBrands.value = known.filter { brand ->
            products.any { it.name.contains(brand, true) }
        }
    }
}

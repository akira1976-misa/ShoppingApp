package com.shopping.pricecompare.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class SubCategoryItem(
    val name: String
)

data class MidCategoryItem(
    val name: String,
    val subs: List<String> = emptyList()
)

data class MainCategoryItem(
    val name: String,
    val mids: List<MidCategoryItem> = emptyList()
)

/**
 * 카테고리 데이터 (대분류 24개 / 중분류 345개 / 소분류 2,599개)
 * assets/categories.json 에서 런타임에 불러옵니다.
 * 데이터가 너무 많아 Kotlin 코드에 직접 작성하면 빌드가 느려지고
 * 파일이 비대해지므로 JSON 에셋 + Gson 파싱 방식을 사용합니다.
 */
object CategoryData {

    private var cachedTree: List<MainCategoryItem>? = null

    /** 최초 호출 시 JSON을 읽어 캐싱, 이후에는 캐시된 값 반환 */
    fun loadTree(context: Context): List<MainCategoryItem> {
        cachedTree?.let { return it }
        val json = try {
            context.assets.open("categories.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            return emptyList()
        }
        val type = object : com.google.gson.reflect.TypeToken<List<MainCategoryItem>>() {}.type
        val tree: List<MainCategoryItem> = try {
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        cachedTree = tree
        return tree
    }

    /** 대분류 이름 목록 */
    fun mainNames(context: Context): List<String> = loadTree(context).map { it.name }

    /** 대분류 이름으로 중분류 목록 조회 */
    fun getMids(context: Context, mainName: String): List<MidCategoryItem> =
        loadTree(context).firstOrNull { it.name == mainName }?.mids ?: emptyList()

    /** 중분류 이름으로 소분류 목록 조회 */
    fun getSubs(context: Context, mainName: String, midName: String): List<String> =
        getMids(context, mainName).firstOrNull { it.name == midName }?.subs ?: emptyList()

    /** 검색 API에 보낼 검색어 생성 (소분류명을 그대로 사용) */
    fun getQuery(mainName: String, midName: String? = null, subName: String? = null): String {
        return subName ?: midName ?: mainName
    }
}

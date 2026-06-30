package com.shopping.pricecompare.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shopping.pricecompare.R
import com.shopping.pricecompare.model.SellerListing
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

/**
 * /skill-routing 적용:
 * - 구매 버튼 클릭 시 정식 https 웹주소로 이동
 *   (쇼핑몰 앱이 설치되어 있고 App Links가 등록되어 있으면 OS가 자동으로 앱으로 연결,
 *    없으면 자동으로 브라우저로 연결됨)
 * - 쿠팡 링크에는 파트너스 수익화 추적 코드(subId)를 자동 포함
 */
class SellerAdapter(
    private val sellers: List<SellerListing>,
    private val productName: String = ""
) : RecyclerView.Adapter<SellerAdapter.VH>() {
    private val fmt = NumberFormat.getNumberInstance(Locale.KOREA)

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val rank = v.findViewById<TextView>(R.id.tv_seller_rank)
        val shop = v.findViewById<TextView>(R.id.tv_seller_shop)
        val price = v.findViewById<TextView>(R.id.tv_seller_price)
        val ship = v.findViewById<TextView>(R.id.tv_seller_shipping)
        val total = v.findViewById<TextView>(R.id.tv_seller_total)
        val btn = v.findViewById<TextView>(R.id.btn_buy)

        fun bind(s: SellerListing, r: Int) {
            rank.text = "$r"; shop.text = s.shopName
            price.text = "${fmt.format(s.price)}원"
            ship.text = if (s.isFreeShipping) "무료" else "${fmt.format(s.shippingFee)}원"
            total.text = "${fmt.format(s.totalPrice)}원"
            if (r == 1) {
                total.setTextColor(itemView.context.getColor(R.color.lowest_price_color))
                itemView.setBackgroundColor(itemView.context.getColor(R.color.lowest_price_bg))
                rank.setBackgroundResource(R.drawable.bg_rank_first)
            } else {
                total.setTextColor(itemView.context.getColor(R.color.text_primary))
                itemView.setBackgroundColor(itemView.context.getColor(android.R.color.transparent))
                rank.setBackgroundResource(R.drawable.bg_rank_normal)
            }
            btn.setOnClickListener {
                val url = buildUrl(s.shopName, s.productUrl, productName)
                openLink(itemView.context, url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_seller, parent, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(sellers[pos], pos + 1)
    override fun getItemCount() = sellers.size

    companion object {

        /** 쿠팡 파트너스 수익화 추적 코드 (앱 식별용 subId) */
        private const val COUPANG_AFFILIATE_SUB_ID = "shopping_app"

        /**
         * /skill-routing: 앱 우선 연결.
         * setPackage를 지정하지 않고 정식 https 주소로 ACTION_VIEW를 실행하면
         * 안드로이드 OS가 App Links 등록 여부를 보고 자동으로
         * 설치된 쇼핑몰 앱 또는 브라우저 중 알맞은 곳을 선택해 연다.
         */
        fun openLink(context: android.content.Context, url: String) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://search.shopping.naver.com/search/all")))
                } catch (e2: Exception) { }
            }
        }

        /**
         * 쇼핑몰별 URL 생성.
         * - 실제 상품 상세 URL이 있으면 그대로 사용
         * - 쿠팡인 경우 파트너스 수익화 subId 파라미터를 자동 추가
         * - 그 외에는 검색 결과 URL 생성
         */
        fun buildUrl(shopName: String, productUrl: String, productName: String): String {
            if (productUrl.isNotBlank() && isProductDetailUrl(productUrl)) {
                return applyAffiliateTracking(shopName, productUrl)
            }
            val q = try { URLEncoder.encode(productName, "UTF-8") } catch (e: Exception) { productName }
            val rawUrl = when {
                shopName.contains("쿠팡") -> "https://www.coupang.com/np/search?q=$q"
                shopName.contains("네이버") -> "https://m.shopping.naver.com/search/all?query=$q"
                shopName.contains("G마켓") -> "https://browse.gmarket.co.kr/search?keyword=$q"
                shopName.contains("11번가") -> "https://m.11st.co.kr/search/Search.tmall?searchKeyword=$q"
                shopName.contains("옥션") -> "https://browse.auction.co.kr/search?keyword=$q"
                shopName.contains("SSG") -> "https://www.ssg.com/search.ssg?target=all&query=$q"
                shopName.contains("롯데") -> "https://www.lotteon.com/search/search/search.ecn?render=search&platform=p&mallId=1&query=$q"
                shopName.contains("위메프") -> "https://search.wemakeprice.com/search?query=$q"
                shopName.contains("마켓컬리") -> "https://www.kurly.com/search?sword=$q"
                shopName.contains("올리브영") -> "https://www.oliveyoung.co.kr/store/search/getSearchList.do?query=$q"
                shopName.contains("무신사") -> "https://www.musinsa.com/search/musinsa/integration?q=$q"
                shopName.contains("교보") -> "https://search.kyobobook.co.kr/search?keyword=$q"
                shopName.contains("예스24") -> "https://www.yes24.com/Product/Search?domain=ALL&query=$q"
                else -> if (productUrl.isNotBlank()) productUrl else "https://search.shopping.naver.com/search/all?query=$q"
            }
            return applyAffiliateTracking(shopName, rawUrl)
        }

        /** /skill-routing: 쿠팡 링크에 수익화(어필리에이트) subId 파라미터를 자동 변환/추가 */
        private fun applyAffiliateTracking(shopName: String, url: String): String {
            if (!shopName.contains("쿠팡")) return url
            if (url.contains("subId=")) return url // 이미 포함된 경우 중복 방지
            val separator = if (url.contains("?")) "&" else "?"
            return "$url${separator}subId=$COUPANG_AFFILIATE_SUB_ID"
        }

        private fun isProductDetailUrl(url: String): Boolean {
            val patterns = listOf("/vp/products/","/catalog/","itemId=","productId=","/product/","/goods/","/item/","smartstore.naver.com")
            return patterns.any { url.contains(it) }
        }
    }
}

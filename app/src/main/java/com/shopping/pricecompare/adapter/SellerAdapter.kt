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

class SellerAdapter(
    private val sellers: List<SellerListing>,
    private val productName: String = ""
) : RecyclerView.Adapter<SellerAdapter.VH>() {

    private val fmt = NumberFormat.getNumberInstance(Locale.KOREA)

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val rank  = v.findViewById<TextView>(R.id.tv_seller_rank)
        val shop  = v.findViewById<TextView>(R.id.tv_seller_shop)
        val price = v.findViewById<TextView>(R.id.tv_seller_price)
        val ship  = v.findViewById<TextView>(R.id.tv_seller_shipping)
        val total = v.findViewById<TextView>(R.id.tv_seller_total)
        val btn   = v.findViewById<TextView>(R.id.btn_buy)

        fun bind(s: SellerListing, r: Int) {
            rank.text  = "$r"
            shop.text  = s.shopName
            price.text = "${fmt.format(s.price)}원"
            ship.text  = if (s.isFreeShipping) "무료" else "${fmt.format(s.shippingFee)}원"
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
        LayoutInflater.from(parent.context).inflate(R.layout.item_seller, parent, false)
    )
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(sellers[pos], pos + 1)
    override fun getItemCount() = sellers.size

    companion object {

        /**
         * 정식 웹 주소로 연결.
         * 쇼핑몰 앱이 설치되어 있고 App Links(공식 도메인 연결)가
         * 등록되어 있으면 안드로이드가 자동으로 해당 앱을 띄워줍니다.
         * 앱이 없으면 자동으로 브라우저가 열립니다.
         * (커스텀 스킴 주소는 사용하지 않습니다 — 실제 존재 여부를 보장할 수 없기 때문)
         */
        fun openLink(context: android.content.Context, url: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                // setPackage를 지정하지 않음 → 안드로이드가 자동으로
                // 설치된 쇼핑몰 앱 또는 브라우저 중 알맞은 곳으로 연결
                context.startActivity(intent)
            } catch (e: Exception) {
                // 혹시 실패하면 네이버쇼핑 검색으로 폴백
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://search.shopping.naver.com/search/all")))
                } catch (e2: Exception) { /* 무시 */ }
            }
        }

        /** 쇼핑몰별 정식 웹 검색 URL 생성 */
        fun buildUrl(shopName: String, productUrl: String, productName: String): String {
            // 실제 상품 상세 URL이면 그대로 사용 (네이버 API 연동 시)
            if (productUrl.isNotBlank() && isProductDetailUrl(productUrl)) {
                return productUrl
            }
            val q = try { URLEncoder.encode(productName, "UTF-8") } catch (e: Exception) { productName }
            return when {
                // 쿠팡 - App Links 가장 적극적으로 등록되어 있어 앱 연결 확률 높음
                shopName.contains("쿠팡")     -> "https://www.coupang.com/np/search?q=$q"
                // 네이버쇼핑 - 모바일 전용 주소 (m.) 사용 시 네이버 앱 인식률 더 높음
                shopName.contains("네이버")   -> "https://m.shopping.naver.com/search/all?query=$q"
                shopName.contains("G마켓")   -> "https://browse.gmarket.co.kr/search?keyword=$q"
                // 11번가 - 모바일 전용 주소(m.11st.co.kr) 사용
                shopName.contains("11번가")  -> "https://m.11st.co.kr/search/Search.tmall?searchKeyword=$q"
                shopName.contains("옥션")    -> "https://browse.auction.co.kr/search?keyword=$q"
                shopName.contains("SSG")     -> "https://www.ssg.com/search.ssg?target=all&query=$q"
                shopName.contains("롯데")    -> "https://www.lotteon.com/search/search/search.ecn?render=search&platform=p&mallId=1&query=$q"
                shopName.contains("위메프")  -> "https://search.wemakeprice.com/search?query=$q"
                shopName.contains("마켓컬리") -> "https://www.kurly.com/search?sword=$q"
                shopName.contains("올리브영") -> "https://www.oliveyoung.co.kr/store/search/getSearchList.do?query=$q"
                shopName.contains("무신사")  -> "https://www.musinsa.com/search/musinsa/integration?q=$q"
                shopName.contains("하이마트") -> "https://www.e-himart.co.kr/app/search/searchMain.himart?query=$q"
                shopName.contains("교보")    -> "https://search.kyobobook.co.kr/search?keyword=$q"
                shopName.contains("예스24")  -> "https://www.yes24.com/Product/Search?domain=ALL&query=$q"
                shopName.contains("알라딘")  -> "https://www.aladin.co.kr/search/wsearchresult.aspx?SearchWord=$q"
                else -> if (productUrl.isNotBlank()) productUrl
                        else "https://search.shopping.naver.com/search/all?query=$q"
            }
        }

        private fun isProductDetailUrl(url: String): Boolean {
            val patterns = listOf(
                "/vp/products/", "/catalog/", "itemId=", "productId=",
                "/product/", "/goods/", "/item/", "smartstore.naver.com"
            )
            return patterns.any { url.contains(it) }
        }
    }
}

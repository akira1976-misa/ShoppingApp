package com.shopping.pricecompare.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.shopping.pricecompare.R
import com.shopping.pricecompare.adapter.SellerAdapter
import com.shopping.pricecompare.databinding.ActivityProductDetailBinding
import com.shopping.pricecompare.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityProductDetailBinding
    private val fmt = NumberFormat.getNumberInstance(Locale.KOREA)

    @Suppress("DEPRECATION")
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        val p = intent.getSerializableExtra("product") as? Product ?: run { finish(); return }

        setSupportActionBar(b.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = p.name }

        setupInfo(p)
        setupSellerList(p)
    }

    private fun setupInfo(p: Product) {
        Glide.with(this).load(p.imageUrl).placeholder(R.drawable.ic_image_placeholder).centerCrop().into(b.imgDetailProduct)
        b.tvDetailProductName.text = p.name
        b.tvDetailCategory.text = p.category
        b.tvDetailRating.text = "★ ${p.rating}  (${fmt.format(p.reviewCount)}개 리뷰)"
        b.tvDetailDescription.text = p.description
        b.tvDetailLowestPrice.text = "${fmt.format(p.lowestTotalPrice)}원"
        b.tvDetailLowestShop.text = "최저가: ${p.lowestSeller?.shopName ?: ""}"
        b.tvSellerCount.text = "총 ${p.sellers.size}개 쇼핑몰 비교"

        if (p.isSpecialDeal) {
            b.tvDetailSpecialBadge.visibility = View.VISIBLE
            b.tvDetailSpecialBadge.text = "🔥 ${p.discountRate}% 특가"
        } else {
            b.tvDetailSpecialBadge.visibility = View.GONE
        }

        b.btnBuyLowest.setOnClickListener {
            val seller = p.lowestSeller ?: return@setOnClickListener
            val url = SellerAdapter.buildUrl(seller.shopName, seller.productUrl, p.name)
            SellerAdapter.openLink(this, url)
        }
    }

    private fun setupSellerList(p: Product) {
        val sorted = p.sellers.sortedBy { it.totalPrice }
        b.rvSellers.apply {
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            adapter = SellerAdapter(sorted, p.name)
            isNestedScrollingEnabled = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}

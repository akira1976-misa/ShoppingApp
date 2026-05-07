package com.shopping.pricecompare.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.shopping.pricecompare.R
import com.shopping.pricecompare.databinding.ActivityProductDetailBinding
import com.shopping.pricecompare.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val product = intent.getSerializableExtra("product") as? Product
            ?: run { finish(); return }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = product.shopName
        }

        setupProductInfo(product)
        setupActionButtons(product)
        loadWebView(product)
    }

    private fun setupProductInfo(product: Product) {
        binding.tvDetailProductName.text = product.name
        binding.tvDetailShopName.text = product.shopName
        binding.tvDetailPrice.text = "${numberFormat.format(product.price)}원"
        binding.tvDetailShipping.text = if (product.isFreeShipping) "무료배송"
        else "배송비 ${numberFormat.format(product.shippingFee)}원"
        binding.tvDetailTotal.text = "합산 ${numberFormat.format(product.totalPrice)}원"
        binding.tvDetailRating.text = "${product.rating} (${numberFormat.format(product.reviewCount)}개 리뷰)"
        binding.tvDetailDescription.text = product.description

        if (product.isSpecialDeal) {
            binding.tvDetailSpecialBadge.isVisible = true
            binding.tvDetailDiscount.isVisible = true
            binding.tvDetailDiscount.text = "-${product.discountRate}% 특가"
        }

        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_image_placeholder)
            .centerCrop()
            .into(binding.imgDetailProduct)
    }

    private fun setupActionButtons(product: Product) {
        binding.btnOpenInBrowser.setOnClickListener {
            if (product.productUrl.isNotBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(product.productUrl)))
            }
        }
        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${product.name}\n가격: ${numberFormat.format(product.price)}원\n${product.productUrl}")
            }
            startActivity(Intent.createChooser(shareIntent, "상품 공유하기"))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(product: Product) {
        if (product.productUrl.isBlank()) {
            binding.webView.isVisible = false
            return
        }
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    view.loadUrl(request.url.toString())
                    return true
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.isVisible = false
                }
            }
            loadUrl(product.productUrl)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

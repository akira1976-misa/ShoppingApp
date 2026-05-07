package com.shopping.pricecompare.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.shopping.pricecompare.R
import com.shopping.pricecompare.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val isHorizontal: Boolean = false
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback()) {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardRoot: CardView         = itemView.findViewById(R.id.card_root)
        private val imgProduct: ImageView      = itemView.findViewById(R.id.img_product)
        private val tvSpecialBadge: TextView   = itemView.findViewById(R.id.tv_special_badge)
        private val tvDiscountRate: TextView   = itemView.findViewById(R.id.tv_discount_rate)
        private val tvProductName: TextView    = itemView.findViewById(R.id.tv_product_name)
        private val tvShopName: TextView       = itemView.findViewById(R.id.tv_shop_name)
        private val tvOriginalPrice: TextView  = itemView.findViewById(R.id.tv_original_price)
        private val tvProductPrice: TextView   = itemView.findViewById(R.id.tv_product_price)
        private val tvShippingFee: TextView    = itemView.findViewById(R.id.tv_shipping_fee)
        private val tvTotalPrice: TextView     = itemView.findViewById(R.id.tv_total_price)
        private val ratingBar: RatingBar       = itemView.findViewById(R.id.rating_bar)
        private val tvRating: TextView         = itemView.findViewById(R.id.tv_rating)
        private val tvReviewCount: TextView    = itemView.findViewById(R.id.tv_review_count)

        fun bind(product: Product) {
            Glide.with(imgProduct.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(imgProduct)

            tvProductName.text = product.name
            tvShopName.text = product.shopName

            if (product.isSpecialDeal) {
                tvSpecialBadge.visibility = View.VISIBLE
                tvDiscountRate.visibility = View.VISIBLE
                tvDiscountRate.text = "-${product.discountRate}%"
                tvOriginalPrice.visibility = View.VISIBLE
                tvOriginalPrice.text = "${numberFormat.format(product.originalPrice)}원"
                tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvSpecialBadge.visibility = View.GONE
                tvDiscountRate.visibility = View.GONE
                tvOriginalPrice.visibility = View.GONE
            }

            tvProductPrice.text = "${numberFormat.format(product.price)}원"

            if (product.isFreeShipping) {
                tvShippingFee.text = "+ 무료배송"
                tvShippingFee.setTextColor(itemView.context.getColor(R.color.free_shipping_color))
            } else {
                tvShippingFee.text = "+ 배송비 ${numberFormat.format(product.shippingFee)}원"
                tvShippingFee.setTextColor(itemView.context.getColor(R.color.shipping_fee_color))
            }

            tvTotalPrice.text = "합산 ${numberFormat.format(product.totalPrice)}원"
            ratingBar.rating = product.rating
            tvRating.text = product.rating.toString()
            tvReviewCount.text = "(${numberFormat.format(product.reviewCount)})"

            cardRoot.setOnClickListener { onProductClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layout = if (isHorizontal) R.layout.item_product_horizontal else R.layout.item_product
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }
}

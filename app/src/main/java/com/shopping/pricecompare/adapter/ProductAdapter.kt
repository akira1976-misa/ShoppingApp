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
    private val isHorizontal: Boolean = false,
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.VH>(Diff()) {
    private val fmt = NumberFormat.getNumberInstance(Locale.KOREA)
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: CardView = v.findViewById(R.id.card_root)
        val img: ImageView = v.findViewById(R.id.img_product)
        val badge: TextView = v.findViewById(R.id.tv_special_badge)
        val disc: TextView = v.findViewById(R.id.tv_discount_rate)
        val name: TextView = v.findViewById(R.id.tv_product_name)
        val shop: TextView = v.findViewById(R.id.tv_shop_name)
        val orig: TextView = v.findViewById(R.id.tv_original_price)
        val price: TextView = v.findViewById(R.id.tv_product_price)
        val ship: TextView = v.findViewById(R.id.tv_shipping_fee)
        val total: TextView = v.findViewById(R.id.tv_total_price)
        val stars: RatingBar = v.findViewById(R.id.rating_bar)
        val rat: TextView = v.findViewById(R.id.tv_rating)
        val rev: TextView = v.findViewById(R.id.tv_review_count)
        fun bind(p: Product) {
            Glide.with(img.context).load(p.imageUrl).placeholder(R.drawable.ic_image_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade()).centerCrop().into(img)
            name.text = p.name; shop.text = p.lowestSeller?.shopName ?: ""
            if (p.isSpecialDeal) {
                badge.visibility = View.VISIBLE; disc.visibility = View.VISIBLE; disc.text = "-${p.discountRate}%"
                orig.visibility = View.VISIBLE; orig.text = "${fmt.format(p.originalPrice)}원"
                orig.paintFlags = orig.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else { badge.visibility = View.GONE; disc.visibility = View.GONE; orig.visibility = View.GONE }
            price.text = "${fmt.format(p.lowestPrice)}원"
            if (p.isFreeShipping) { ship.text = "+ 무료배송"; ship.setTextColor(itemView.context.getColor(R.color.free_shipping_color)) }
            else { ship.text = "+ 배송비 ${fmt.format(p.lowestShipping)}원"; ship.setTextColor(itemView.context.getColor(R.color.text_secondary)) }
            total.text = "최저 ${fmt.format(p.lowestTotalPrice)}원"
            stars.rating = p.rating; rat.text = p.rating.toString(); rev.text = "(${fmt.format(p.reviewCount)})"
            card.setOnClickListener { onProductClick(p) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layout = if (isHorizontal) R.layout.item_product_horizontal else R.layout.item_product
        return VH(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }
    override fun onBindViewHolder(holder: VH, position: Int) { holder.bind(getItem(position)) }
    class Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
        override fun areContentsTheSame(a: Product, b: Product) = a == b
    }
}

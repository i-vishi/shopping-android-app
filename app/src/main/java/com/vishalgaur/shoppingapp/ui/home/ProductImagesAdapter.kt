package com.vishalgaur.shoppingapp.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vishalgaur.shoppingapp.databinding.ImagesItemBinding

class ProductImagesAdapter(private val context: Context, private val images: List<String>) :
    RecyclerView.Adapter<ProductImagesAdapter.ViewHolder>() {

    class ViewHolder(binding: ImagesItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.rcImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ImagesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]
        val imgUrl = imageUrl.toUri().buildUpon().scheme("https").build()

        Glide.with(context)
            .asBitmap()
            .load(imgUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size
}
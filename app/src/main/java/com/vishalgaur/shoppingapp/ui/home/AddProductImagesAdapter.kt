package com.vishalgaur.shoppingapp.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vishalgaur.shoppingapp.databinding.AddImagesItemBinding

class AddProductImagesAdapter(private val images: List<Uri>) :
    RecyclerView.Adapter<AddProductImagesAdapter.ViewHolder>() {

    class ViewHolder(binding: AddImagesItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imgView = binding.addImagesImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AddImagesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = images[position]
        holder.imgView.setImageURI(imageUrl)
    }

    override fun getItemCount(): Int = images.size
}
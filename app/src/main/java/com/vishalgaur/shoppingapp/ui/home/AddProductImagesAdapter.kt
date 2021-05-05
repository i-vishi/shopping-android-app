package com.vishalgaur.shoppingapp.ui.home

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vishalgaur.shoppingapp.databinding.AddImagesItemBinding

class AddProductImagesAdapter(private val context: Context, images: List<Uri>) :
    RecyclerView.Adapter<AddProductImagesAdapter.ViewHolder>() {

    private var data: MutableList<Uri> = images as MutableList<Uri>

    inner class ViewHolder(private var binding: AddImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: Uri, pos: Int) {
            binding.addImgCloseBtn.setOnClickListener {
                deleteItem(pos)
            }

            if (imgUrl.toString().contains("https://")) {
                Glide.with(context)
                    .asBitmap()
                    .load(imgUrl.buildUpon().scheme("https").build())
                    .into(binding.addImagesImageView)
            } else {
                binding.addImagesImageView.setImageURI(imgUrl)
            }
        }
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
        val imageUrl = data[position]
        holder.bind(imageUrl, position)
    }

    override fun getItemCount(): Int = data.size

    fun deleteItem(index: Int) {
        data.removeAt(index)
        notifyDataSetChanged()
    }

}
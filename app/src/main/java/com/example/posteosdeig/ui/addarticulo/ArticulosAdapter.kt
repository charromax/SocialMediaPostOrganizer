package com.example.posteosdeig.ui.addarticulo

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.databinding.ItemLayoutBinding
import com.example.posteosdeig.util.Categories

class ArticulosAdapter(private val context: Context) :
    ListAdapter<Articulo, ArticulosAdapter.ArticulosViewHolder>(ArticleCallback()) {

    inner class ArticulosViewHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(articulo: Articulo) {
            binding.apply {
                text1.text = articulo.title
                text1.background.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        Categories.valueOf(articulo.category).color
                    ), PorterDuff.Mode.SRC_ATOP
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticulosViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticulosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticulosViewHolder, position: Int) {
        val currentArticle = getItem(position)
        holder.bind(currentArticle)
    }

    class ArticleCallback : DiffUtil.ItemCallback<Articulo>() {
        override fun areItemsTheSame(
            oldItem: Articulo,
            newItem: Articulo
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Articulo,
            newItem: Articulo
        ): Boolean {
            return oldItem == newItem
        }
    }
}
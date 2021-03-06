package com.example.posteosdeig.ui.addeditcoleccion

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

class AvailableArticulosAdapter(
    private val listener: OnClickListener,
    private val context: Context
) : ListAdapter<Articulo, AvailableArticulosAdapter.AvailableArticulosViewHolder>(ArticleCallback()) {

    inner class AvailableArticulosViewHolder(
        private val binding: ItemLayoutBinding,
        parent: ViewGroup
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onArticleSelected(getItem(position), position, parent.id)

                    }
                }
            }
        }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableArticulosViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AvailableArticulosViewHolder(binding, parent)
    }

    override fun onBindViewHolder(holder: AvailableArticulosViewHolder, position: Int) {
        val currentArticle = getItem(position)
        holder.bind(currentArticle)
    }

    fun removeArticle(position: Int) {
        val newList = ArrayList<Articulo>(currentList)
        newList.removeAt(position)
        submitList(newList)
    }

    fun clearList() {
        val newList = ArrayList<Articulo>(currentList)
        newList.clear()
        submitList(newList)
    }

    fun addArticle(articulo: Articulo) {
        val newList = ArrayList<Articulo>(currentList)
        newList.add(articulo)
        submitList(newList)
        notifyDataSetChanged()
    }

    fun updateList(list: List<Articulo>) {
        val newList = ArrayList<Articulo>(list)
        submitList(newList)
        notifyDataSetChanged()
    }

    interface OnClickListener {
        fun onArticleSelected(articulo: Articulo, position: Int, parentId: Int)
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
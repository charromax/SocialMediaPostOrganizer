package com.example.posteosdeig.ui.colecciones

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import com.example.posteosdeig.databinding.CollectionItemBinding

class ColeccionesAdapter(private val context: Context) :
    ListAdapter<ColeccionWithArticulos, ColeccionesAdapter.ColeccionesViewHolder>(DiffCallback()) {

    inner class ColeccionesViewHolder(private val binding: CollectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(col: ColeccionWithArticulos) {
            binding.apply {
                with(col) {
                    titleText.text = coleccion.name
                    creationDate?.text = coleccion.formattedDate
                }

            }
            val articleAdapter = ArticulosAdapter(context)
            with(binding.articlesList){
                adapter = articleAdapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            articleAdapter.submitList(col.article)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColeccionesViewHolder {
        val binding = CollectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColeccionesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColeccionesViewHolder, position: Int) {
        val currentCollection = getItem(position)
        holder.bind(currentCollection)
    }

    class DiffCallback : DiffUtil.ItemCallback<ColeccionWithArticulos>() {
        override fun areItemsTheSame(
            oldItem: ColeccionWithArticulos,
            newItem: ColeccionWithArticulos
        ): Boolean {
            return oldItem.coleccion.id == newItem.coleccion.id
        }

        override fun areContentsTheSame(
            oldItem: ColeccionWithArticulos,
            newItem: ColeccionWithArticulos
        ): Boolean {
            return oldItem == newItem
        }

    }
}
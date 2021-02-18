package com.example.posteosdeig.ui.colecciones

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.posteosdeig.R
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import com.example.posteosdeig.databinding.CollectionItemBinding
import com.example.posteosdeig.ui.addarticulo.ArticulosAdapter

class ColeccionesAdapter(private val context: Context, private val listener: OnClickListener) :
    ListAdapter<ColeccionWithArticulos, ColeccionesAdapter.ColeccionesViewHolder>(DiffCallback()) {

    inner class ColeccionesViewHolder(private val binding: CollectionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCollectionSelected(getItem(position))

                    }
                }

            }
        }

        fun bind(col: ColeccionWithArticulos) {
            binding.apply {

                with(col) {
                    titleText.text = coleccion.name
                    creationDate?.text = coleccion.formattedDate
                    branchLabel.text = coleccion.branch
                    if (coleccion.isMarked && coleccion.isSent.not()) {
                        isMarkedIcon.visibility = View.VISIBLE
                        isMarkedIcon.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.delete_color
                            ), PorterDuff.Mode.SRC_ATOP
                        )
                    } else if (coleccion.isMarked.not() && coleccion.isSent) {
                        isMarkedIcon.visibility = View.VISIBLE
                        isMarkedIcon.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                R.color.sport_color
                            ), PorterDuff.Mode.SRC_ATOP
                        )
                    }
                }

            }

            val articleAdapter = ArticulosAdapter(context)
            with(binding.articlesList) {
                adapter = articleAdapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            articleAdapter.submitList(col.article)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColeccionesViewHolder {
        val binding =
            CollectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColeccionesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColeccionesViewHolder, position: Int) {
        val currentCollection = getItem(position)
        holder.bind(currentCollection)
    }

    interface OnClickListener {
        fun onCollectionSelected(coleccionWithArticulos: ColeccionWithArticulos)
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
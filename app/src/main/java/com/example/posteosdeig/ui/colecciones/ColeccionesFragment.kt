package com.example.posteosdeig.ui.colecciones

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.posteosdeig.R
import com.example.posteosdeig.data.SortOrder
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import com.example.posteosdeig.databinding.FragmentCollectionsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ColeccionesFragment : Fragment(R.layout.fragment_collections) {
    private val REQUEST_CODE = 101
    private val viewModel: ColeccionesViewModel by viewModels()
    private var removedPosition = 0
    private var removedCol: ColeccionWithArticulos? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCollectionsBinding.bind(view)
        val coleccionesAdapter = ColeccionesAdapter()

        binding.apply {
            collectionsList.apply {
                adapter = coleccionesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

        }
        viewModel.colecciones.observe(viewLifecycleOwner) { coleccionesAdapter.submitList(it) }
        setHasOptionsMenu(true)

        val touchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val col = coleccionesAdapter.currentList[viewHolder.adapterPosition]

                if (direction == ItemTouchHelper.RIGHT) {
                    removedCol = col
                    removedPosition = viewHolder.adapterPosition
                    viewModel.onDeleteCollection(col)
                    coleccionesAdapter.notifyItemRemoved(removedPosition)
                    Snackbar.make(
                        viewHolder.itemView,
                        "${col.coleccion.name} borrada!",
                        Snackbar.LENGTH_LONG
                    ).setAction("Deshacer") {
                        //TODO: add col back in DB and update articles with col_id
                        removedCol?.let {
                            viewModel.saveNewCollection(it.coleccion)
                            viewModel.addArticlesBackInCollection(it)
                        }
                    }.show()

                } else if (direction == ItemTouchHelper.LEFT) {
                    removedCol = col
                    removedPosition = viewHolder.adapterPosition
                    viewModel.onReleaseArticles(col)
                    coleccionesAdapter.notifyItemChanged(removedPosition)
                    Snackbar.make(
                        viewHolder.itemView,
                        "${col.coleccion.name} Articulos liberados!",
                        Snackbar.LENGTH_LONG
                    ).setAction("Deshacer") {
                        removedCol?.let { viewModel.addArticlesBackInCollection(it) }
                    }.show()
                }
            }

        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.collectionsList)

        binding.addArticle.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.add_article_dialog)
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar_menu, menu)
        val newCollection = menu.findItem(R.id.add_article)
        val sortByDate = menu.findItem(R.id.action_sort_by_date)
        val sortByName = menu.findItem(R.id.action_sort_by_name)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_collection -> {
                findNavController().navigate(R.id.add_collection_dialog)
                true
            }
            R.id.read_file -> {
                openFile()
                true
            }
            R.id.action_sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            var currentUri: Uri? = null
            data?.data?.let {
                if (viewModel.onFileRead(it)) {
                    Snackbar.make(
                        requireView(),
                        "Articulos agregados!",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    Snackbar.make(
                        requireView(),
                        "Error agregando articulos :(",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        }
    }
}
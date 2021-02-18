package com.example.posteosdeig.ui.colecciones

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.posteosdeig.R
import com.example.posteosdeig.data.SortOrder
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import com.example.posteosdeig.databinding.FragmentCollectionsBinding
import com.example.posteosdeig.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.collect

const val sMAIL = "manuelrg88@gmail.com"
const val sPWD = "Mg412115"

@AndroidEntryPoint
class ColeccionesFragment : Fragment(R.layout.fragment_collections),
    ColeccionesAdapter.OnClickListener {
    private val REQUEST_CODE = 101
    private val viewModel: ColeccionesViewModel by viewModels()
    private var removedPosition = 0
    private var removedCol: ColeccionWithArticulos? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCollectionsBinding.bind(view)
        val coleccionesAdapter = ColeccionesAdapter(requireContext(), this)

        binding.apply {
            collectionsList.apply {
                adapter = coleccionesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

        }
        viewModel.colecciones.observe(viewLifecycleOwner) {
            coleccionesAdapter.submitList(it)
        }
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
                    viewModel.onDeleteCollection(col)
                    coleccionesAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                } else if (direction == ItemTouchHelper.LEFT) {
                    removedCol = col
                    removedPosition = viewHolder.adapterPosition
                    viewModel.onReleaseArticles(col)
                    coleccionesAdapter.notifyItemChanged(removedPosition)

                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftActionIcon(R.drawable.ic_release)
                    .addSwipeRightActionIcon(R.drawable.ic_delete_forever_24)
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.sport_color
                        )
                    )
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.delete_color
                        )
                    )
                    .create()
                    .decorate()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.collectionsList)

        binding.addArticle.setOnClickListener(View.OnClickListener {
            findNavController().navigate(R.id.add_article_dialog)
        })

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.colEvents.collect { event ->
                when (event) {

                    is ColeccionesViewModel.ColeccionesEvents.ShowUndoDeleteCollectionMessage -> {
                        Snackbar.make(
                            binding.root,
                            "${event.col.coleccion.name} borrada!",
                            Snackbar.LENGTH_LONG
                        ).setAction(getString(R.string.undo)) {
                            viewModel.onUndoDelete(event.col)
                        }.show()
                    }

                    is ColeccionesViewModel.ColeccionesEvents.ShowUndoFreeArticlesMessage -> {
                        Snackbar.make(
                            binding.root,
                            "${event.col.coleccion.name} Articulos liberados!",
                            Snackbar.LENGTH_LONG
                        ).setAction(getString(R.string.undo)) {
                            viewModel.addArticlesBackInCollection(event.col)
                        }.show()
                    }
                    is ColeccionesViewModel.ColeccionesEvents.NavigateToAddCollectionFragment -> {
                        val action =
                            ColeccionesFragmentDirections.actionColeccionesFragmentToAddCollection()
                        findNavController().navigate(action)
                    }
                    is ColeccionesViewModel.ColeccionesEvents.NavigateToEditCollectionFragment -> {
                        val action =
                            ColeccionesFragmentDirections.actionColeccionesFragmentToAddCollection(
                                event.col.coleccion, event.col.article.toTypedArray()
                            )
                        findNavController().navigate(action)
                    }
                    ColeccionesViewModel.ColeccionesEvents.CollectionAdded -> {
                        Snackbar.make(
                            binding.root,
                            "Coleccion agregada a la lista",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }.exhaustive
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.action_bar_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_collection -> {
                viewModel.onAddNewCollectionClick()
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
                        getString(R.string.articles_added_message),
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_adding_articles_message),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    override fun onCollectionSelected(coleccionWithArticulos: ColeccionWithArticulos) {
        viewModel.onCollectionSelected(coleccionWithArticulos)
    }

}
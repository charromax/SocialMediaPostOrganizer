package com.example.posteosdeig.ui.addeditcoleccion

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.posteosdeig.R
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Branches
import com.example.posteosdeig.databinding.FragmentAddCollectionBinding
import com.example.posteosdeig.util.Categories
import com.example.posteosdeig.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class AddCollectionFragment : Fragment(R.layout.fragment_add_collection),
    AvailableArticulosAdapter.OnClickListener {

    private val _TAG = "ADDCOLLECTION"
    private val viewModel: AddEditCollectionViewModel by viewModels()
    private var articlesList = arrayListOf<Articulo>()
    private lateinit var availableArticulosAdapter: AvailableArticulosAdapter
    private lateinit var selectedArticulosAdapter: AvailableArticulosAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddCollectionBinding.bind(view)
        viewModel.getAllAvailableArticles(Categories.MODA)
        availableArticulosAdapter = AvailableArticulosAdapter(this, requireContext())
        selectedArticulosAdapter = AvailableArticulosAdapter(this, requireContext())

        with(binding) {
            availableArticlesList.apply {
                adapter = availableArticulosAdapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            selectedArticlesList.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = selectedArticulosAdapter
            }
            categoriesSpin.adapter =
                ArrayAdapter<Categories>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    Categories.values()
                )
            branchesSpin.adapter =
                ArrayAdapter<Branches>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    Branches.values()
                )
            viewModel.availableArticles.observe(viewLifecycleOwner, Observer {
                availableArticulosAdapter.submitList(it)
            })
            binding.apply {
                collectionNameText.setText(viewModel.colName)
                creationDateText.text = viewModel.dateCreated
            }
            selectedArticulosAdapter.updateList(viewModel.articlesInCollection.toList())
            viewModel.onReleaseArticles(viewModel.articlesInCollection.toList())
            articlesList.addAll(viewModel.articlesInCollection)

            categoriesSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (parent != null) {
                        viewModel.onCategorySelected(parent.getItemAtPosition(position) as Categories)
                        availableArticulosAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }

            branchesSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (parent != null) {

                        viewModel.branch = parent.getItemAtPosition(position).toString()
                        availableArticulosAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    viewModel.branch = Branches.EIFFEL.name
                }

            }

            collectionNameText.addTextChangedListener {
                viewModel.colName = it.toString()
            }
            saveCollection.setOnClickListener {
                selectedArticulosAdapter.clearList()
                viewModel.onSaveClick(articlesList)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.colEvents.collect { event ->
                when (event) {
                    AddEditCollectionViewModel.AddColeccionesEvents.ArticlesReleasedWarning -> {
                        Snackbar.make(
                            binding.root,
                            "Articulos liberados, guardar antes de salir",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is AddEditCollectionViewModel.AddColeccionesEvents.CollectionSavedMessage -> {
                        Snackbar.make(
                            binding.root,
                            "${event.coleccion.name} guardada exitosamente!",
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.collectionNameText.setText("")
                    }
                    AddEditCollectionViewModel.AddColeccionesEvents.ShowNoTitleMessage -> {
                        Snackbar.make(
                            binding.root,
                            "No olvides ponerle nombre!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }.exhaustive
            }
        }

    }

    override fun onArticleSelected(articulo: Articulo, position: Int, parentId: Int) {
        if (parentId == R.id.available_articles_list) {
            Log.i(_TAG, "onArticleSelected: $parentId")
            articlesList.add(articulo)
            availableArticulosAdapter.removeArticle(position)
            selectedArticulosAdapter.addArticle(articulo)
        } else {
            articlesList.remove(articulo)
            selectedArticulosAdapter.removeArticle(position)
            availableArticulosAdapter.addArticle(articulo)
        }
    }
}
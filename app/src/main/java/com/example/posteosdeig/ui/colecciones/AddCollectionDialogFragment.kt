package com.example.posteosdeig.ui.colecciones

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.posteosdeig.R
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Coleccion
import com.example.posteosdeig.databinding.FragmentAddCollectionBinding
import com.example.posteosdeig.util.Categories
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat


@AndroidEntryPoint
class AddCollectionDialogFragment : Fragment(R.layout.fragment_add_collection),
    AvailableArticulosAdapter.OnClickListener {

    private val _TAG = "ADDCOLLECTION"
    private val viewModel: ColeccionesViewModel by viewModels()
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
            viewModel.availableArticles.observe(viewLifecycleOwner, Observer {
                availableArticulosAdapter.submitList(it)
            })
            categoriesSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                    if (parent != null) {
                        viewModel.onCategorySelected(parent.getItemAtPosition(position) as Categories)
                        availableArticulosAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
            creationDateText.text = "Coleccion creada el ${DateFormat.getDateTimeInstance().format(System.currentTimeMillis())}"
            saveCollection.setOnClickListener{
                val coleccion = Coleccion(
                    name = binding.collectionNameText.text.toString().trim()
                )

                viewModel.saveNewCollection(coleccion)
                articlesList.map {
                    val updateArticulo = it.copy(collectionId = coleccion.id)
                    viewModel.addArticleToCollection(updateArticulo)
                }
                collectionNameText.setText("")
                Snackbar.make(
                    view,
                    "Coleccion ${coleccion.name} guardada con exito!",
                    Snackbar.LENGTH_LONG
                ).show()
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
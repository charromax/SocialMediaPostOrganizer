package com.example.posteosdeig.ui.colecciones

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
class AddCollectionDialogFragment: DialogFragment(R.layout.fragment_add_collection), AvailableArticulosAdapter.OnClickListener {

    private val viewModel: ColeccionesViewModel by viewModels()
    private var articlesList = arrayListOf<Articulo>()
    private lateinit var articulosAdapter: AvailableArticulosAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding= FragmentAddCollectionBinding.bind(view)
        viewModel.getAllAvailableArticles(Categories.MODA)
        articulosAdapter = AvailableArticulosAdapter(this)

        with(binding) {
            availableArticlesList.apply {
                adapter = articulosAdapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            }
            categoriesSpin.adapter =
                ArrayAdapter<Categories>(requireContext(), R.layout.item_layout, Categories.values())
            viewModel.availableArticles.observe(viewLifecycleOwner, Observer {
                articulosAdapter.submitList(it)
            })
            categoriesSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                    if (parent != null) {
                        viewModel.onCategorySelected(parent.getItemAtPosition(position) as Categories)
                        articulosAdapter.notifyDataSetChanged()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
            creationDateText.text = "Coleccion creada el ${DateFormat.getDateTimeInstance().format(System.currentTimeMillis())}"
            saveCollection.setOnClickListener{
                val coleccion = Coleccion(
                    name = binding.collectionNameText.text.toString().trim(),
                    category = binding.categoriesSpin.selectedItem.toString()
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



    override fun onArticleSelected(articulo: Articulo, position: Int) {
        articlesList.add(articulo)
        articulosAdapter.removeArticle(position)
    }
}
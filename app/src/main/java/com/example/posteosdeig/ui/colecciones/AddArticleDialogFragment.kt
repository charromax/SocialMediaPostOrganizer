package com.example.posteosdeig.ui.colecciones

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.posteosdeig.R
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.databinding.FragmentAddArticleBinding
import com.example.posteosdeig.util.Categories
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddArticleDialogFragment : DialogFragment(R.layout.fragment_add_article) {
    private val viewModel: ColeccionesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddArticleBinding.bind(view)
        binding.categoriesSpin.adapter =
            ArrayAdapter<Categories>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Categories.values()
            )
        binding.saveArticle.setOnClickListener(View.OnClickListener {
            //TODO: Save new article to DB
            viewModel.addNewArticle(
                Articulo(
                    title = binding.articleNameText.text.toString().trim(),
                    category = binding.categoriesSpin.selectedItem.toString()
                )
            )
            Snackbar.make(binding.root, "Articulo guardado!", Snackbar.LENGTH_SHORT).show()
            binding.articleNameText.setText("")
        })
    }

}
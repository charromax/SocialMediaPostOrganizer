package com.example.posteosdeig.ui.addeditcoleccion

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.posteosdeig.data.ArticlesDao
import com.example.posteosdeig.data.PreferencesManager
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Coleccion
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import com.example.posteosdeig.util.Categories
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DateFormat

class AddEditCollectionViewModel @ViewModelInject constructor(
    private val articlesDao: ArticlesDao,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val applicationContext: Context,
    @Assisted private val state: SavedStateHandle

) : ViewModel() {
    private val prefs = preferencesManager.preferencesFlow
    private val addColsEventChannel = Channel<AddEditCollectionViewModel.AddColeccionesEvents>()
    val colEvents = addColsEventChannel.receiveAsFlow()

    private var availableArticlesFlow: Flow<List<Articulo>> =
        prefs.flatMapLatest { getAllAvailableArticles(it.category) }
    val availableArticles = availableArticlesFlow.asLiveData()
    var coleccion = state.get<ColeccionWithArticulos>("coleccion")
    var colName = state.get<ColeccionWithArticulos>("coleccion")?.coleccion?.name
        ?: coleccion?.coleccion?.name ?: ""
        set(value) {
            field = value
            state.set("coleccion", value)
        }

    var dateCreated = state.get<ColeccionWithArticulos>("coleccion")?.coleccion?.formattedDate
        ?: coleccion?.coleccion?.formattedDate ?: "Coleccion creada el ${
            DateFormat.getDateTimeInstance().format(System.currentTimeMillis())
        }"

    var articlesInCollection =
        state.get<ColeccionWithArticulos>("coleccion")?.article ?: coleccion?.article ?: emptyList()
        set(value) {
            field = value
            state.set("article", value)
        }

    fun getAllAvailableArticles(category: Categories): Flow<List<Articulo>> {
        return if (category != Categories.TODAS) {
            articlesDao.getAllArticlesForCategory(category.name)
        } else {
            articlesDao.getAllArticlesAvailable()
        }
    }

    private fun showNoTitleError() = viewModelScope.launch {
        addColsEventChannel.send(AddColeccionesEvents.ShowNoTitleMessage)
    }

    fun onCategorySelected(category: Categories) = viewModelScope.launch {
        preferencesManager.updateCategory(category)
    }

    private fun addArticleToCollection(articulo: Articulo) = viewModelScope.launch {
        articlesDao.addArticleToCollection(articulo)
    }

    private fun saveNewCollection(coleccion: Coleccion) = viewModelScope.launch {
        articlesDao.insertCol(coleccion)
        addColsEventChannel.send(AddColeccionesEvents.CollectionSavedMessage(coleccion))
    }

    private fun updateCollection(updatedCollection: Coleccion) = viewModelScope.launch {
        articlesDao.updateCollection(updatedCollection)
        addColsEventChannel.send(AddColeccionesEvents.CollectionSavedMessage(updatedCollection))
    }

    fun onReleaseArticles(list: List<Articulo>) = viewModelScope.launch {
        list.map {
            articlesDao.removeFromCollection(it.copy(collectionId = ""))
        }
        addColsEventChannel.send(AddColeccionesEvents.ArticlesReleasedWarning)
    }

    fun onSaveClick(articlesList: List<Articulo>) {
        if (colName.isBlank()) {
            showNoTitleError()
            return
        }
        if (coleccion != null) {
            coleccion?.coleccion?.copy(name = colName)?.let { updateCollection(it) }
            articlesList.map { addArticleToCollection(it.copy(collectionId = coleccion?.coleccion?.id!!)) }
        } else {
            val newCol = Coleccion(name = colName)
            saveNewCollection(newCol)
            articlesList.map { addArticleToCollection(it.copy(collectionId = newCol.id)) }
        }

    }


    sealed class AddColeccionesEvents {
        object ShowNoTitleMessage : AddColeccionesEvents()
        object ArticlesReleasedWarning : AddColeccionesEvents()
        data class CollectionSavedMessage(val coleccion: Coleccion) : AddColeccionesEvents()
    }


}
package com.example.posteosdeig.ui.colecciones

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.posteosdeig.data.ArticlesDao
import com.example.posteosdeig.data.PreferencesManager
import com.example.posteosdeig.data.SortOrder
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Coleccion
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

const val TAG = "VIEWMODEL"
class ColeccionesViewModel @ViewModelInject constructor(
    private val articlesDao: ArticlesDao,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val applicationContext: Context

    ) : ViewModel() {
    private val prefs = preferencesManager.preferencesFlow
    private val coleccionesFlow =
        prefs.flatMapLatest { articlesDao.getSortedCollectionsWithArticles(it.order) }
    val colecciones = coleccionesFlow.asLiveData()

    private val coleccionesEventChannel = Channel<ColeccionesEvents>()
    val colEvents = coleccionesEventChannel.receiveAsFlow()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onDeleteCollection(col: ColeccionWithArticulos) = viewModelScope.launch {
        onReleaseArticles(col, false)
        articlesDao.deleteCollection(col.coleccion)
        coleccionesEventChannel.send(ColeccionesEvents.ShowUndoDeleteCollectionMessage(col))
    }

    fun onReleaseArticles(col: ColeccionWithArticulos, shouldShow: Boolean = true) =
        viewModelScope.launch {
            col.article.map {
                articlesDao.removeFromCollection(it.copy(collectionId = ""))
            }
            if (shouldShow) coleccionesEventChannel.send(
                ColeccionesEvents.ShowUndoFreeArticlesMessage(
                    col
                )
            )
        }

    fun addArticlesBackInCollection(col:ColeccionWithArticulos) = viewModelScope.launch {
        col.article.map {
            articlesDao.addArticleToCollection(it.copy(collectionId = col.coleccion.id))
        }
    }

    fun addNewArticle(articulo: Articulo) = viewModelScope.launch {
        articlesDao.insertArt(articulo)
    }

    private fun saveNewCollection(coleccion: Coleccion) = viewModelScope.launch {
        articlesDao.insertCol(coleccion)
    }

    private fun getListData(uri: Uri): List<String>? {
        return try{
            val file = applicationContext.contentResolver.openInputStream(uri)
            val inputStreamReader = InputStreamReader(file)
            BufferedReader(inputStreamReader).readLines()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun onFileRead(uri: Uri): Boolean{
        val articlesList = getListData(uri)
        articlesList?.map {
            val parts = it.split(',')
            val articulo = Articulo(title = parts[0], category = parts[1])
            Log.i(TAG, "Saving article: ${articulo.toString()}")
            addNewArticle(articulo)
        }
        return true
    }

    fun onUndoDelete(col: ColeccionWithArticulos) {
        saveNewCollection(col.coleccion)
        addArticlesBackInCollection(col)
    }

    fun onAddNewCollectionClick() = viewModelScope.launch {
        coleccionesEventChannel.send(ColeccionesEvents.NavigateToAddCollectionFragment)
    }

    fun onCollectionSelected(col: ColeccionWithArticulos) = viewModelScope.launch {
        coleccionesEventChannel.send(ColeccionesEvents.NavigateToEditCollectionFragment(col))
    }

    sealed class ColeccionesEvents {
        data class ShowUndoFreeArticlesMessage(val col: ColeccionWithArticulos) :
            ColeccionesEvents()

        data class ShowUndoDeleteCollectionMessage(val col: ColeccionWithArticulos) :
            ColeccionesEvents()

        object NavigateToAddCollectionFragment : ColeccionesEvents()
        data class NavigateToEditCollectionFragment(val col: ColeccionWithArticulos) :
            ColeccionesEvents()
    }

}


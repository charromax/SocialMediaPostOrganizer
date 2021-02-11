package com.example.posteosdeig.ui.colecciones

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContentResolverCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.posteosdeig.data.ArticlesDao
import com.example.posteosdeig.data.FilterPreferences
import com.example.posteosdeig.data.PreferencesManager
import com.example.posteosdeig.data.SortOrder
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Coleccion
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import com.example.posteosdeig.util.Categories
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
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
    private var availableArticlesFlow : Flow<List<Articulo>> = prefs.flatMapLatest { getAllAvailableArticles(it.category) }
    private val coleccionesFlow = prefs.flatMapLatest { articlesDao.getSortedCollectionsWithArticles(it.order) }
    val colecciones = coleccionesFlow.asLiveData()
    val availableArticles = availableArticlesFlow.asLiveData()


    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }
    fun onCategorySelected(category: Categories) = viewModelScope.launch {
        preferencesManager.updateCategory(category)
    }

    fun onDeleteCollection(col:ColeccionWithArticulos) = viewModelScope.launch {
        onReleaseArticles(col)
        articlesDao.deleteCollection(col.coleccion)
    }

    fun onReleaseArticles(col:ColeccionWithArticulos) = viewModelScope.launch {
        col.article.map {
            articlesDao.removeFromCollection(it.copy(collectionId = ""))
        }
    }

    fun addArticlesBackInCollection(col:ColeccionWithArticulos) = viewModelScope.launch {
        col.article.map {
            articlesDao.addArticleToCollection(it.copy(collectionId = col.coleccion.id))
        }
    }

    fun addNewArticle(articulo: Articulo) = viewModelScope.launch {
        articlesDao.insertArt(articulo)
    }

    fun getAllAvailableArticles(category: Categories): Flow<List<Articulo>> {
            return articlesDao.getAllArticlesForCategory(category.name)
    }

    fun saveNewCollection(coleccion: Coleccion) = viewModelScope.launch {
        articlesDao.insertCol(coleccion)
    }

    fun addArticleToCollection(articulo: Articulo) = viewModelScope.launch {
        articlesDao.addArticleToCollection(articulo)
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



}


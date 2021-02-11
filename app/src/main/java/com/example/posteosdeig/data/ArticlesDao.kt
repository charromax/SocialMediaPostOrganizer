package com.example.posteosdeig.data

import androidx.room.*
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Coleccion
import com.example.posteosdeig.data.model.ColeccionWithArticulos
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticlesDao {

    fun getSortedCollectionsWithArticles(order: SortOrder): Flow<List<ColeccionWithArticulos>> =
        when(order) {
            SortOrder.BY_DATE -> getCollectionsWithArticlesByDate()
            SortOrder.BY_NAME -> getCollectionsWithArticlesByName()
        }

    @Transaction
    @Query("SELECT * FROM collections_table ORDER BY createdAt DESC")
    fun getCollectionsWithArticlesByDate(): Flow<List<ColeccionWithArticulos>>

    @Transaction
    @Query("SELECT * FROM collections_table ORDER BY name")
    fun getCollectionsWithArticlesByName(): Flow<List<ColeccionWithArticulos>>

    @Query("SELECT * FROM collections_table")
    fun getAllCollections(): Flow<List<Coleccion>>

    @Query("SELECT * FROM article_table WHERE category=:category AND collectionId = ''")
    fun getAllArticlesForCategory(category: String): Flow<List<Articulo>>

    @Update
    suspend fun addArticleToCollection(article: Articulo)

    @Update
    suspend fun removeFromCollection(article: Articulo)

    @Delete
    suspend fun deleteArticle(article: Articulo)

    @Delete
    suspend fun deleteCollection(collection: Coleccion)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArt(article:Articulo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCol(collection:Coleccion)



}
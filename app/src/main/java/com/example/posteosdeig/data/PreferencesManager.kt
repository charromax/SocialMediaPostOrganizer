package com.example.posteosdeig.data

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.example.posteosdeig.util.Categories
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreferences(
    val order: SortOrder ,
    val category: Categories)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("user_prefs")
    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val sortOrder = SortOrder.valueOf(
                prefs[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val selectedCategory = Categories.valueOf(
                prefs[PreferencesKeys.SELECTED_CATEGORY] ?: Categories.TODAS.name
            )
            FilterPreferences(sortOrder, selectedCategory)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateCategory(category: Categories) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_CATEGORY] = category.name
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val SELECTED_CATEGORY = preferencesKey<String>("selected_category")
    }



}
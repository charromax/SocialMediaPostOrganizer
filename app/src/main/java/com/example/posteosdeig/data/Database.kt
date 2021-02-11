package com.example.posteosdeig.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.posteosdeig.data.model.Articulo
import com.example.posteosdeig.data.model.Coleccion
import com.example.posteosdeig.di.ApplicationScope
import com.example.posteosdeig.util.Categories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Articulo::class, Coleccion::class], version = 1)
abstract class Database : RoomDatabase() {

    abstract fun articlesDao(): ArticlesDao

    class Callback @Inject constructor(
        private val database: Provider<com.example.posteosdeig.data.Database>,

        @ApplicationScope
        private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

        }
    }

}
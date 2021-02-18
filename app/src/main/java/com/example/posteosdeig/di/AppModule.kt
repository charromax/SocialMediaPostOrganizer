package com.example.posteosdeig.di

import android.app.Application
import androidx.room.Room
import com.example.posteosdeig.data.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application, callback: Database.Callback) =
        Room.databaseBuilder(app, Database::class.java, "articles_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideArticlesDao(db: Database) = db.articlesDao()


    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
package com.example.posteosdeig

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.posteosdeig.data.ArticlesDao
import com.example.posteosdeig.di.EmailWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var articlesDao: ArticlesDao

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(EmailWorkerFactory(articlesDao))
            .build()

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(applicationContext, workManagerConfiguration)
    }
}
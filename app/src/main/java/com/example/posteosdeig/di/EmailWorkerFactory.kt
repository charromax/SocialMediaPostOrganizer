package com.example.posteosdeig.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.posteosdeig.data.ArticlesDao

class EmailWorkerFactory(private val articlesDao: ArticlesDao) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return EmailWorker(
            appContext,
            workerParameters,
            articlesDao
        )
    }
}
package com.example.posteosdeig.di

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.posteosdeig.data.ArticlesDao
import com.example.posteosdeig.util.JavaMailAPI
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@HiltWorker
class EmailWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val articlesDao: ArticlesDao
) : CoroutineWorker(context, params) {
    companion object {
        const val TAG = "WORKER"
        const val ZONE_ID = "America/Argentina/Buenos_Aires"
        const val DEFAULT_MESSAGE =
            "Buenos dias, los articulos para postear esta semana van a ser los siguientes:\n\n\n"
        const val RECEIVER_EMAIL = "manuelrg88@gmail.com"
        const val SUBJECT = "Colecciones para postear"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        Log.i(TAG, "doWork: worker started...")
        var message =
            DEFAULT_MESSAGE
        val today = ZonedDateTime.now(ZoneId.of(ZONE_ID))

        if (today.dayOfWeek == DayOfWeek.MONDAY) {
            val list = articlesDao.getCollectionsMarked()
            val allCollections = articlesDao.getCollectionsWithArticlesByName()

            if (list.isNullOrEmpty().not()) {
                list.forEach { col ->
                    val postDate = Instant.ofEpochMilli(col.coleccion.postDate).atZone(
                        ZoneId.of(
                            ZONE_ID
                        )
                    )
                    if (postDate.dayOfYear == today.dayOfYear) {
                        message += (col.toString() + "\n")
                        articlesDao.updateCollection(
                            col.coleccion.copy(
                                isMarked = false,
                                isSent = true
                            )
                        )
                        Log.i(TAG, "doWork: coleccion updated")
                    }
                }
                // Article recycling

                allCollections.collect { cols ->
                    if (cols.isNotEmpty()) {
                        cols.forEach {
                            val creationDate = Instant.ofEpochMilli(it.coleccion.postDate).atZone(
                                ZoneId.of(ZONE_ID)
                            )
                            if (today.dayOfYear - creationDate.dayOfYear >= 60) {
                                it.article.forEach { art ->
                                    Log.i(TAG, "doWork: releasing articles")
                                    articlesDao.removeFromCollection(art)
                                }
                            }

                        }
                    }
                }
            } else {
                Log.i(TAG, "doWork: list empty")
                return Result.failure()
            }

        } else {
            Log.i(TAG, "doWork: not Today")
            return Result.failure()
        }

        if (message.length > 83) {      // length of default string message

            JavaMailAPI.sendMail(
                applicationContext,
                SUBJECT,
                message,
                RECEIVER_EMAIL
            )
            Log.i(TAG, "doWork: email sent")
            return Result.success()
        }
        return Result.failure()
    }
}
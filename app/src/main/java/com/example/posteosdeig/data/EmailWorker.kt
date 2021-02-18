package com.example.posteosdeig.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.posteosdeig.util.JavaMailAPI
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
        var message = DEFAULT_MESSAGE
        val today = ZonedDateTime.now(ZoneId.of(ZONE_ID))

        if (today.dayOfWeek == DayOfWeek.THURSDAY) {
            val list = articlesDao.getCollectionsMarked()

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
            } else {
                Log.i(TAG, "doWork: list empty")
                return Result.failure()
            }

        } else {
            Log.i(TAG, "doWork: not Today")
            return Result.failure()
        }

        if (message.length > 83) {

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
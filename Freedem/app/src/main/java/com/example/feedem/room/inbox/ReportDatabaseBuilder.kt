package com.example.feedem.room.inbox

import android.content.Context
import androidx.room.Room

object ReportDatabaseBuilder {

    private var INSTANCE: ReportDatabase? = null

    fun getInstance(context: Context): ReportDatabase {
        if (INSTANCE == null) {
            synchronized(ReportDatabase::class) {
                INSTANCE = buildReportRoomDb(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildReportRoomDb(context: Context) = Room.databaseBuilder(
        context.applicationContext,
        ReportDatabase::class.java,
        "feedem-report"
    ).build()

}
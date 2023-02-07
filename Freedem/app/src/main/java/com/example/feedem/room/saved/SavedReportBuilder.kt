package com.example.feedem.room.saved

import android.content.Context
import androidx.room.Room
import com.exclr8.n1reportmanagement.room.saved.SavedReportDatabase

object SavedReportBuilder {
    private var INSTANCE: SavedReportDatabase? = null

    fun getInstance(context: Context): SavedReportDatabase {
        if (INSTANCE == null) {
            synchronized(SavedReportDatabase::class) {
                INSTANCE = buildSavedReportRoomDb(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildSavedReportRoomDb(context: Context) = Room.databaseBuilder(
        context.applicationContext,
        SavedReportDatabase::class.java,
        "feedem-saved-report"
    ).build()
}
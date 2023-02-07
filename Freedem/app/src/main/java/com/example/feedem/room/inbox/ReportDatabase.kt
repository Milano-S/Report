package com.example.feedem.room.inbox

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feedem.model.ReportData


@Database(entities = [ReportData::class], version = 2, exportSchema = false)
abstract class ReportDatabase: RoomDatabase() {
    abstract fun ReportDao(): ReportDao
}
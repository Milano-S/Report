package com.exclr8.n1reportmanagement.room.saved

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feedem.model.SavedReportData
import com.example.feedem.room.saved.SavedReportDao

@Database(entities = [SavedReportData::class], version = 1, exportSchema = false)
abstract class SavedReportDatabase: RoomDatabase() {
    abstract fun SavedReportDao(): SavedReportDao
}
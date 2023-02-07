package com.example.feedem.room.saved

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.feedem.model.SavedReportData

@Dao
interface SavedReportDao {

    @Query("SELECT * FROM SavedReportData")
    suspend fun getSavedReports(): List<SavedReportData>

    @Query("SELECT * FROM SavedReportData WHERE DocumentKey = :DocumentKey")
    suspend fun getSavedReport(DocumentKey: String): SavedReportData

    @Query("Delete from SavedReportData WHERE DocumentKey = :DocumentKey")
    suspend fun deleteReport(DocumentKey: String)

    @Query("SELECT * FROM SavedReportData WHERE DocumentKey = :DocumentKey")
    suspend fun getSavedReportUrl(DocumentKey: String): SavedReportData

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveReport(Report: SavedReportData)

    @Query("Delete from SavedReportData")
    suspend fun deleteAll()
}
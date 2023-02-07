package com.example.feedem.room.inbox

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.feedem.model.ReportData

@Dao
interface ReportDao {

    @Query("SELECT * FROM ReportData")
    suspend fun getAllReports(): List<ReportData>

    @Query("SELECT * FROM ReportData WHERE DocumentUrl = :url")
    suspend fun getReportFromUrl(url: String): ReportData

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllReports(Reports: List<ReportData>)

    @Query("SELECT * from ReportData WHERE isRead")
    fun getReadItem(): ReportData

    @Query("Delete from ReportData")
    suspend fun deleteAll()

    @Update
    suspend fun updateReport(Report: ReportData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReport(Report: ReportData)

    @Query("DELETE FROM ReportData WHERE DocumentKey = :docKey")
    suspend fun deleteReport(docKey: String)
}
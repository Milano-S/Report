package com.example.feedem.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReportData(
    @ColumnInfo(name = "BranchId") val BranchId: Int,
    @ColumnInfo(name = "BranchName") val BranchName: String,
    @ColumnInfo(name = "DateTitle") val DateTitle: String,
    @PrimaryKey @ColumnInfo(name = "DocumentKey") val DocumentKey: String,
    @ColumnInfo(name = "DocumentUrl") val DocumentUrl: String,
    @ColumnInfo(name = "FinancialPeriodId") val FinancialPeriodId: Int,
    @ColumnInfo(name = "PeriodType") val PeriodType: String,
    @ColumnInfo(name = "ReportId") val ReportId: Int,
    @ColumnInfo(name = "ReportName") val ReportName: String,
    @ColumnInfo(name = "savedLocation") val savedLocation: String?,
    @ColumnInfo(name = "isRead") var isRead: Boolean = false,
    @ColumnInfo(name = "isSaved") var isSaved: Boolean = false
)

package com.example.feedem.model

data class DocumentResponse(
    val Exception: Any,
    val Offline: Boolean,
    val Reports: List<ReportData>,
    val Success: Boolean
)
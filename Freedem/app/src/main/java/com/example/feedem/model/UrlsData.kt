package com.example.feedem.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UrlsData(
    //@PrimaryKey val primaryKey: Int? = null,
    @PrimaryKey @ColumnInfo(name = "Url") val baseUrl: String
)
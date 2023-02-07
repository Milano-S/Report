package com.example.feedem.room.url

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.feedem.model.UrlsData

@Dao
interface UrlDao {

    @Query("SELECT * FROM UrlsData")
    suspend fun getAllBaseUrls(): List<UrlsData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUrl(baseUrl: UrlsData)

    @Query("DELETE FROM UrlsData")
    suspend fun deleteAllUrls()
}

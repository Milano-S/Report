package com.example.feedem.room.url

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.feedem.model.UrlsData

@Database(entities = [UrlsData::class], version = 1 , exportSchema = false)
abstract class UrlDatabase : RoomDatabase(){
    abstract fun UrlDao(): UrlDao
}
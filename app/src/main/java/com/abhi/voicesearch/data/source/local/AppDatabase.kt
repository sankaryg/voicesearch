package com.abhi.voicesearch.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abhi.voicesearch.data.App

/**
 * Inspired from Architecture Components MVVM sample app
 */
@Database(entities = [App::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun snapsDao(): AppsDao


}
